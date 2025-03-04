package org.rs2server.cache;

public class Cache {

    public static void init() {
        try {
            CacheManager.load(CacheConstants.PATH);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    public static int getAmountOfItems() {
    	return CacheManager.getContainerChildCount(2, 10);
    }

    public static int getAmountOfObjects() {
        return CacheManager.getContainerChildCount(2, 6);
    }

    public static int getAmountOfInterfaces() {
        return CacheManager.containerCount(CacheConstants.INTERFACEDEF_IDX_ID);
    }

    public static int getAmountOfNpcs() {
    	return CacheManager.getRealContainerChildCount(2, 9);
    }

    public static int getAmountOfGraphics() {
        return CacheManager.cacheCFCount(CacheConstants.GFX_IDX_ID);
    }

    public static int getAmountOfAnims() {
        return CacheManager.cacheCFCount2(CacheConstants.ANIM_IDX_ID);
    }

}
