package com.slt.template.seqlib;

public final class SequenceManager {


    public static void main(String[] args) {
        System.out.println(
                SequenceManager.getTargetName("WFS")
        );
    }

    public static String getTargetName(String targetSystem, String cid, String payload, String ownerSystem){
        return getTargetName(targetSystem);
    }

    public static String getTargetName(String targetSystem){
        return "SVM/DEV/" + targetSystem + "/CMN/00";
    }
}
