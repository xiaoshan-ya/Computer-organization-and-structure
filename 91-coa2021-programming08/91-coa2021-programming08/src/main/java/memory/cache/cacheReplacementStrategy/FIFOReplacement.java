package memory.cache.cacheReplacementStrategy;

import memory.Memory;
import memory.cache.Cache;

/**
 * TODO 先进先出算法
 */
public class FIFOReplacement implements ReplacementStrategy {

    @Override
    /**
     * 结合具体的替换策略，进行命中后进行相关操作
     * @param rowNO 行号
     */
    public void hit(int rowNO) {
    }

    @Override
    /**
     * 结合具体的映射策略，在给定范围内对cache中的数据进行替换
     * @param start 起始行行号
     * @param end 结束行 闭区间
     * @param addrTag tag
     * @param input  数据
     */
    public int replace(int start, int end, char[] addrTag, char[] input) {
        Cache cache = Cache.getCache();
        if (start >= 1024 && end > 1024) { // 针对关联映射而言
            start = 0;
            end = 1024;
        }
        int minTimeStamp_row = start;
        for (int i = start; i < end; i++){
            if (!cache.isValid(i)){ // 有空闲行
                minTimeStamp_row = i;
                break;
            }
            if (cache.getTimeStamp(i) < cache.getTimeStamp(minTimeStamp_row)) {
                minTimeStamp_row = i;
                break;
            }
        }
        cache.changeTimeStamp(minTimeStamp_row);
        cache.update(minTimeStamp_row, addrTag, input);
        return minTimeStamp_row;
    }

}
