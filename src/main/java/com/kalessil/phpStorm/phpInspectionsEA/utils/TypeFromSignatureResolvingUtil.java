package com.kalessil.phpStorm.phpInspectionsEA.utils;

import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.*;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

final public class TypeFromSignatureResolvingUtil {

    static public void resolveSignature (
            @Nullable String signatureToResolve,
            @Nullable Function scope,
            @NotNull PhpIndex projectIndex,
            @NotNull Set<String> extractedTypes
    ) {
        Set<String> processedSignatures = new HashSet<>();
        resolveSignature(signatureToResolve, scope, projectIndex, extractedTypes, processedSignatures);
        processedSignatures.clear();
    }

    static private void resolveSignature (
            @Nullable String strSignatureToResolve,
            @Nullable Function objScope,
            @NotNull PhpIndex objIndex,
            @NotNull Set<String> extractedTypesSet,
            @NotNull Set<String> processedSignatures
    ) {
        /* do nothing with empty signatures and already processed signatures */
        if (StringUtils.isEmpty(strSignatureToResolve) || processedSignatures.contains(strSignatureToResolve)) {
            return;
        }
        processedSignatures.add(strSignatureToResolve);

        /* re-dispatch poly-variants to single-variant processing */
        if (strSignatureToResolve.contains("|")) {
            for (String strOneVariantFromSplitToResolve : strSignatureToResolve.split("\\|")) {
                resolveSignature(strOneVariantFromSplitToResolve, objScope, objIndex, extractedTypesSet, processedSignatures);
            }
            return;
        }

        /* skip primitive types */
        if (strSignatureToResolve.charAt(0) != '#' && strSignatureToResolve.charAt(0) != '?') {
            extractedTypesSet.add(Types.getType(strSignatureToResolve));
            return;
        }


        char charTypeOfSignature = ((strSignatureToResolve.length()) >= 2 ? strSignatureToResolve.charAt(1) : '?');
        if (
            charTypeOfSignature == 'D' || /* pre-defined constants type is not resolved */
            charTypeOfSignature == '?' || /* have no idea what does it mean */
            charTypeOfSignature == 'A' || /* parameter type is not resolved */
            charTypeOfSignature == 'E'    /* array item type is not resolved */
        ) {
            /* do nothing here */
            return;
        }

        /* resolve functions */
        if (charTypeOfSignature == 'F') {
            String strFunctionName = strSignatureToResolve.replace("#F", "");
            Collection<Function> objFunctionsCollection = objIndex.getFunctionsByName(strFunctionName);
            for (Function objFunction : objFunctionsCollection) {
                /*
                 * infinity loop was discovered for drupal 7 (drupal_find_base_themes)
                 * IDE for some reason resolved type including self-reference of this function
                 */
                String strTypeWithoutLoop = objFunction.getType().toString().replace("#F" + strFunctionName, "");
                resolveSignature(strTypeWithoutLoop, objScope, objIndex, extractedTypesSet, processedSignatures);
            }
            objFunctionsCollection.clear();

            return;
        }

        /* resolve params and scope variables */
        if (charTypeOfSignature == 'V'){
            String strParameterOrVariableName = strSignatureToResolve.replace("#V", "");
            if (null != objScope) {
                for (Parameter objParam : objScope.getParameters()) {
                    if (objParam.getName().equals(strParameterOrVariableName)) {
                        resolveSignature(objParam.getType().toString(), objScope, objIndex, extractedTypesSet, processedSignatures);
                        return;
                    }
                }
            }

            /* TODO: resolve local vars - find assignments */

            return;
        }

        /* classes and core types */
        if (charTypeOfSignature == 'C') {
            String typeName = strSignatureToResolve.replace("#C", "");
            if (objScope instanceof Method && typeName.equals("static")) {
                final PhpClass container = ((Method) objScope).getContainingClass();
                if (container != null) {
                    typeName = container.getFQN();
                }
            }
            extractedTypesSet.add(Types.getType(typeName));
            return;
        }

        /*
         * Ok, woodoo wizards musts envy here, we are checking classes structures
         * while getting thought execution chain, until we run into poly-variants
         * or missing annotations.
         **/
        final boolean isProperty = (charTypeOfSignature == 'P');
        final boolean isMethod   = (charTypeOfSignature == 'M');
        final boolean isConstant = (charTypeOfSignature == 'K');
        if (isProperty || isMethod || isConstant) {
            String[] arrInternalsAndChain = strSignatureToResolve.split("#C");
            /* { <some garbage>, <target chain> }  */
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

                /* start point for resolving */
                if (null == strClassResolved) {
                    strClassResolved = strSlot;
                    continue;
                }

                /* resolve pair */
                typesOfSlotSet = resolveSlot(strClassResolved, strSlot, objIndex, charTypeOfSignature);


                /*
                 * That's actually a hack, but assumption was correct for real-world frameworks.
                 * if mixed is in mid of resolving, ignore it for poly-variants, so possibly more issues will be found
                 */
                intCountNotMixedTypes = typesOfSlotSet.size();
                for (String strOne :typesOfSlotSet) {
                    /* skip mixed types */
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
                /* finished handling poly-variants with mixed in mid of chain */


                /* what was resolved and becomes class */
                strClassResolved = strFirstTypeResolved;

                /* break on poly-variant/no-variant in middle of chain */
                isPolyVariant = (intCountNotMixedTypes > 1);
                if (!isLastPair && (isPolyVariant || StringUtils.isEmpty(strClassResolved))) {
                    typesOfSlotSet.add(Types.strClassNotResolved);
                    return;
                }

                /* break looking up if pair is not resolved */
                //noinspection ConstantConditions
                if (strClassResolved.equals(Types.strClassNotResolved)) {
                    typesOfSlotSet.add(Types.strClassNotResolved);
                    return;
                }
            }


            if (null != typesOfSlotSet && typesOfSlotSet.size() > 0) {
                /* store resolved types by re-running resolving */
                for (String strType : typesOfSlotSet) {
                    resolveSignature(strType, objScope, objIndex, extractedTypesSet, processedSignatures);
                }
                typesOfSlotSet.clear();
            }
        }
    }

    private static HashSet<String> resolveSlot(String strClass, String strSlot, PhpIndex objIndex, char type) {
        HashSet<String> resolvedTypesSet = new HashSet<>();

        /* try resolving an object interface */
        Collection<PhpClass> objClasses = PhpIndexUtil.getObjectInterfaces(strClass, objIndex, false);
        if (0 == objClasses.size() || StringUtils.isEmpty(strSlot)) {
            resolvedTypesSet.add(Types.strClassNotResolved);
            return resolvedTypesSet;
        }

        /* iterate methods and properties to match slot name */
        boolean isSlotFound;
        String strTypeExtracted;

        char charSlotFirst = strSlot.charAt(0);
        for (PhpClass objClass : objClasses) {
            isSlotFound = false;

            if (type == 'M') {
                for (Method objMethod : objClass.getMethods()) {
                    String strMethodName = objMethod.getName();

                    /* match first chars and then complete names */
                    if (!StringUtils.isEmpty(strMethodName) && strMethodName.charAt(0) == charSlotFirst && strMethodName.equals(strSlot)) {
                         /* exclude recursive definition in own signature */
                        strTypeExtracted = objMethod
                                .getType().toString()
                                .replace("#" + type + "#C" + strClass + "." + strSlot, "");

                        Collections.addAll(resolvedTypesSet, strTypeExtracted.split("\\|"));

                        isSlotFound = true;
                        break;
                    }
                }
                if (isSlotFound) {
                    continue;
                }
            }

            if (type == 'P' || type == 'K') {
                for (Field objField : objClass.getFields()) {
                    String strFieldName = objField.getName();

                    /* match first chars and then complete names */
                    if (!StringUtils.isEmpty(strFieldName) && strFieldName.charAt(0) == charSlotFirst && strFieldName.equals(strSlot)) {
                        /* exclude recursive definition in own signature */
                        strTypeExtracted = objField
                                .getType().toString()
                                .replace("#" + type + "#C" + strClass + "." + strSlot, "");

                        Collections.addAll(resolvedTypesSet, strTypeExtracted.split("\\|"));
                        break;
                    }
                }
            }
        }


        /* can be resolved in several classes - often duplicated ones, or not resolved at all */
        if (resolvedTypesSet.size() == 0) {
            resolvedTypesSet.add(Types.strClassNotResolved);
        }

        return resolvedTypesSet;
    }
}
