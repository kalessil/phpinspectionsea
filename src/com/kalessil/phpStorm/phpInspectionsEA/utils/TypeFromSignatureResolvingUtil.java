package com.kalessil.phpStorm.phpInspectionsEA.utils;

import com.intellij.openapi.util.text.StringUtil;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.Field;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;

import java.util.*;

public class TypeFromSignatureResolvingUtil {

    static public void resolveSignature (String strSignature, PhpIndex objIndex, LinkedList<String> objTypesExtracted) {
        /** do nothing with empty signatures */
        if (StringUtil.isEmpty(strSignature)) {
            return;
        }

        /** re-dispatch poly-variants */
        if (strSignature.contains("|")) {
            for (String strOneVariant : strSignature.split("\\|")) {
                resolveSignature(strOneVariant, objIndex, objTypesExtracted);
            }
            return;
        }

        if (
            strSignature.startsWith("#D") || /** pre-defined constants type is not resolved */
            strSignature.equals("?")      || /** have no idea what does it mean */
            strSignature.startsWith("#A") || /** parameter type is not resolved */
            strSignature.startsWith("#E")    /** array item type is not resolved */
        ) {
            /** do nothing here */
            return;
        }

        /** TODO: implement */
        if (strSignature.startsWith("#F")) {
            /** try resolving function name */
            return;
        }
        /** TODO: implement */
        if (strSignature.startsWith("#V")){
            /** try resolving as parameter name, also it's local scope variables */
            return;
        }

        /** classes and core types */
        if (strSignature.startsWith("#C")) {
            objTypesExtracted.add(Types.getType(strSignature.replace("#C", "")));
            return;
        }

        /**
         * Ok, woodoo wizards musts envy here, we are checking classes structures
         * while getting thought execution chain, until we run into poly-variants
         * or missing annotations.
         **/
        final boolean isProperty = strSignature.startsWith("#P");
        final boolean isMethod = strSignature.startsWith("#M");
        final boolean isConstant = strSignature.startsWith("#K");
        if (isProperty || isMethod || isConstant) {
            String[] arrInternalsAndChain = strSignature.split("#C");
            /** { <some garbage>, <target chain> }  */
            if (arrInternalsAndChain.length != 2) {
                return;
            }

            String[] arrChain = arrInternalsAndChain[1].split("\\.");
            String strClassResolved = null;

            List<String> listTypesOfSlot = null;
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
                listTypesOfSlot = resolveSlot(strClassResolved, strSlot, objIndex);


                /** if mixed is in mid of resolving, ignore it for poly-variants, so possibly more issues will be found */
                intCountNotMixedTypes = listTypesOfSlot.size();
                for (String strOne :listTypesOfSlot) {
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
                    listTypesOfSlot.add(Types.strClassNotResolved);
                    return;
                }

                /** break if pair is not resolved */
                if (strClassResolved.equals(Types.strClassNotResolved)) {
                    listTypesOfSlot.add(Types.strClassNotResolved);
                    return;
                }
            }

            /** re-run resolving on resolved chain types */
            //noinspection ConstantConditions
            for (String strType : listTypesOfSlot) {
                resolveSignature(strType, objIndex, objTypesExtracted);
            }
            return;
        }


        /** here pure core types are passing through */
        objTypesExtracted.add(Types.getType(strSignature));
    }

    static public List<String> resolveSlot(String strClass, String strSlot, PhpIndex objIndex) {
        List<String> listTypesResolved = new LinkedList<>();

        /** try searching classes */
        Collection<PhpClass> objClasses = objIndex.getClassesByName(strClass);
        if (objClasses.size() == 0) {
            objClasses.addAll(objIndex.getClassesByFQN(strClass));
        }
        /** try looking up into interfaces */
        if (objClasses.size() == 0) {
            objClasses.addAll(objIndex.getInterfacesByName(strClass));
        }
        if (objClasses.size() == 0) {
            objClasses.addAll(objIndex.getInterfacesByFQN(strClass));
        }

        /** terminate execution if nothing was found */
        if (objClasses.size() == 0) {
            listTypesResolved.add(Types.strClassNotResolved);
            return listTypesResolved;
        }

        /** iterate methods and properties for slat name match */
        boolean isSlotFound;
        String strTypeExtracted;
        for (PhpClass objClass : objClasses) {
            isSlotFound = false;

            for (Method objMethod : objClass.getMethods()) {
                if (objMethod.getName().equals(strSlot)) {
                    strTypeExtracted = objMethod.getType().toString();
                    Collections.addAll(listTypesResolved, strTypeExtracted.split("\\|"));

                    isSlotFound = true;
                    break;
                }
            }
            if (isSlotFound) {
                continue;
            }

            for (Field objField : objClass.getFields()) {
                if (objField.getName().equals(strSlot)) {
                    strTypeExtracted = objField.getType().toString();
                    Collections.addAll(listTypesResolved, strTypeExtracted.split("\\|"));

                    break;
                }
            }
        }


        /** can be resolved in several classes - often duplicated ones, or not resolved at all */
        if (listTypesResolved.size() == 0) {
            listTypesResolved.add(Types.strClassNotResolved);
        }
        List<String> listUniqueSignatures = new ArrayList<>(new HashSet<>(listTypesResolved));

        listTypesResolved.clear();
        return listUniqueSignatures;
    }
}
