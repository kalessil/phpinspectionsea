package com.kalessil.phpStorm.phpInspectionsEA.utils;

import com.intellij.openapi.util.text.StringUtil;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.*;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

public class TypeFromSignatureResolvingUtil {

    static public void resolveSignature (String strSignatureToResolve, @Nullable Function objScope, PhpIndex objIndex, HashSet<String> extractedTypesSet) {
        /** do nothing with empty signatures */
        if (StringUtil.isEmpty(strSignatureToResolve)) {
            return;
        }

        /** re-dispatch poly-variants to single-variant processing */
        if (strSignatureToResolve.contains("|")) {
            for (String strOneVariantFromSplitToResolve : strSignatureToResolve.split("\\|")) {
                resolveSignature(strOneVariantFromSplitToResolve, objScope, objIndex, extractedTypesSet);
            }
            return;
        }

        /** skip primitive types */
        if (strSignatureToResolve.charAt(0) != '#' && strSignatureToResolve.charAt(0) != '?') {
            extractedTypesSet.add(Types.getType(strSignatureToResolve));
            return;
        }


        char charTypeOfSignature = ((strSignatureToResolve.length()) >= 2 ? strSignatureToResolve.charAt(1) : '?');
        if (
            charTypeOfSignature == 'D' || /** pre-defined constants type is not resolved */
            charTypeOfSignature == '?' || /** have no idea what does it mean */
            charTypeOfSignature == 'A' || /** parameter type is not resolved */
            charTypeOfSignature == 'E'    /** array item type is not resolved */
        ) {
            /** do nothing here */
            return;
        }

        /** resolve functions */
        if (charTypeOfSignature == 'F') {
            String strFunctionName = strSignatureToResolve.replace("#F", "");
            Collection<Function> objFunctionsCollection = objIndex.getFunctionsByName(strFunctionName);
            for (Function objFunction : objFunctionsCollection) {
                /**
                 * infinity loop was discovered for drupal 7 (drupal_find_base_themes)
                 * IDE for some reason resolved type including self-reference of this function
                 */
                String strTypeWithoutLoop = objFunction.getType().toString().replace("#F" + strFunctionName, "");
                resolveSignature(strTypeWithoutLoop, objScope, objIndex, extractedTypesSet);
            }
            objFunctionsCollection.clear();

            return;
        }

        /** resolve params and scope variables */
        if (charTypeOfSignature == 'V'){
            String strParameterOrVariableName = strSignatureToResolve.replace("#V", "");
            if (null != objScope) {
                for (Parameter objParam : objScope.getParameters()) {
                    if (objParam.getName().equals(strParameterOrVariableName)) {
                        resolveSignature(objParam.getType().toString(), objScope, objIndex, extractedTypesSet);
                        return;
                    }
                }
            }

            /** TODO: resolve local vars - find assignments */

            return;
        }

        /** classes and core types */
        if (charTypeOfSignature == 'C') {
            extractedTypesSet.add(Types.getType(strSignatureToResolve.replace("#C", "")));
            return;
        }

        /**
         * Ok, woodoo wizards musts envy here, we are checking classes structures
         * while getting thought execution chain, until we run into poly-variants
         * or missing annotations.
         **/
        final boolean isProperty = (charTypeOfSignature == 'P');
        final boolean isMethod   = (charTypeOfSignature == 'M');
        final boolean isConstant = (charTypeOfSignature == 'K');
        if (isProperty || isMethod || isConstant) {
            String[] arrInternalsAndChain = strSignatureToResolve.split("#C");
            /** { <some garbage>, <target chain> }  */
            if (arrInternalsAndChain.length != 2) {
                return;
            }

            String[] arrChain = arrInternalsAndChain[1].split("\\.");
            String strClassResolved = null;

            HashSet<String> typesOfSlotSet = null;
            boolean isLastPair;
            boolean isPolyVariant;

            String strFirstTypeResolved = null;
            int intCountNotMixedTypes;

            int intCountItemsToProcess = arrChain.length;
            for (String strSlot : arrChain) {
                -- intCountItemsToProcess;
                isLastPair = (intCountItemsToProcess == 0);

                /** start point for resolving */
                if (null == strClassResolved) {
                    strClassResolved = strSlot;
                    continue;
                }

                /** resolve pair */
                typesOfSlotSet = resolveSlot(strClassResolved, strSlot, objIndex);


                /**
                 * That's actually a hack, but assumption was correct for real-world frameworks.
                 * if mixed is in mid of resolving, ignore it for poly-variants, so possibly more issues will be found
                 */
                intCountNotMixedTypes = typesOfSlotSet.size();
                for (String strOne :typesOfSlotSet) {
                    /** skip mixed types */
                    if (
                        !isLastPair && intCountNotMixedTypes > 1 &&
                        Types.getType(strOne).equals(Types.strMixed)
                    ) {
                        --intCountNotMixedTypes;
                        continue;
                    }

                    if (null == strFirstTypeResolved) {
                        strFirstTypeResolved = strOne;
                    }
                }
                /** finished handling poly-variants with mixed in mid of chain */


                /** what was resolved and becomes class */
                strClassResolved = strFirstTypeResolved;

                /** break on poly-variant/no-variant in middle of chain */
                isPolyVariant = (intCountNotMixedTypes > 1);
                if (!isLastPair && (isPolyVariant || StringUtil.isEmpty(strClassResolved))) {
                    typesOfSlotSet.add(Types.strClassNotResolved);
                    return;
                }

                /** break looking up if pair is not resolved */
                //noinspection ConstantConditions
                if (strClassResolved.equals(Types.strClassNotResolved)) {
                    typesOfSlotSet.add(Types.strClassNotResolved);
                    return;
                }
            }


            if (null != typesOfSlotSet) {
                /** store resolved types by re-running resolving */
                for (String strType : typesOfSlotSet) {
                    resolveSignature(strType, objScope, objIndex, extractedTypesSet);
                }
                typesOfSlotSet.clear();
            }
        }
    }

    static public HashSet<String> resolveSlot(String strClass, String strSlot, PhpIndex objIndex) {
        HashSet<String> resolvedTypesSet = new HashSet<String>();

        /** try resolving an object interface */
        Collection<PhpClass> objClasses = PhpIndexUtil.getObjectInterfaces(strClass, objIndex);
        if (objClasses.size() == 0) {
            resolvedTypesSet.add(Types.strClassNotResolved);
            return resolvedTypesSet;
        }

        /** iterate methods and properties to match slot name */
        boolean isSlotFound;
        String strTypeExtracted;

        char charSlotFirst = strSlot.charAt(0);
        for (PhpClass objClass : objClasses) {
            isSlotFound = false;

            for (Method objMethod : objClass.getMethods()) {
                String strMethodName = objMethod.getName();

                /** match first chars and then complete names */
                if (!StringUtil.isEmpty(strMethodName) && strMethodName.charAt(0) == charSlotFirst && strMethodName.equals(strSlot)) {
                    strTypeExtracted = objMethod.getType().toString();
                    Collections.addAll(resolvedTypesSet, strTypeExtracted.split("\\|"));

                    isSlotFound = true;
                    break;
                }
            }
            if (isSlotFound) {
                continue;
            }

            for (Field objField : objClass.getFields()) {
                String strFieldName = objField.getName();

                /** match first chars and then complete names */
                if (!StringUtil.isEmpty(strFieldName) && strFieldName.charAt(0) == charSlotFirst && strFieldName.equals(strSlot)) {
                    strTypeExtracted = objField.getType().toString();
                    Collections.addAll(resolvedTypesSet, strTypeExtracted.split("\\|"));

                    break;
                }
            }
        }


        /** can be resolved in several classes - often duplicated ones, or not resolved at all */
        if (resolvedTypesSet.size() == 0) {
            resolvedTypesSet.add(Types.strClassNotResolved);
        }

        return resolvedTypesSet;
    }
}
