package memory.cache.cacheReplacementStrategy;

import memory.cache.Cache;

/**
 * TODO 最近不经常使用算法
 */
public class LFUReplacement implements ReplacementStrategy {

    @Override
    public void hit(int rowNO) {
        Cache cache = Cache.getCache();
        cache.addVisited(rowNO);
    }

    @Override
    public int replace(int start, int end, char[] addrTag, char[] input) {
        Cache cache = Cache.getCache();
        if (start >= 1024 && end > 1024) {
            start = 0;
            end = 1024;
        }
        int minVisit_row = start;
        for (int i = start; i < end; i++){
            if (cache.getVisited(i) < cache.getVisited(minVisit_row) && cache.isValid(i)) minVisit_row = i;
            if (!cache.isValid(i)){ // 有空闲行
                minVisit_row = i;
                break;
            }
        }
        cache.update(minVisit_row, addrTag, input);
        cache.setVisited(minVisit_row);
        return minVisit_row;
    }

}
