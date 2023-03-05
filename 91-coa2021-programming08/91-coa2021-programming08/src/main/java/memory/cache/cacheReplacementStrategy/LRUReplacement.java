package memory.cache.cacheReplacementStrategy;

import memory.cache.Cache;

/**
 * TODO 最近最少用算法
 */
public class LRUReplacement implements ReplacementStrategy {

    @Override
    public void hit(int rowNO) {
        Cache cache = Cache.getCache();
        cache.changeTimeStamp(rowNO);
    }

    @Override
    public int replace(int start, int end, char[] addrTag, char[] input) {
        Cache cache = Cache.getCache();
        if (start >= 1024 && end > 1024) {
            start = 0;
            end = 1024;
        }
        int minTime_row = start;
        for (int i = start; i < end; i++){
            if (!cache.isValid(i)){
                minTime_row = i;
                break;
            }
            if (cache.getTimeStamp(i) < cache.getTimeStamp(minTime_row)) minTime_row = i;
        }
        cache.update(minTime_row, addrTag, input);
        cache.changeTimeStamp(minTime_row);
        return minTime_row;
    }

}





























