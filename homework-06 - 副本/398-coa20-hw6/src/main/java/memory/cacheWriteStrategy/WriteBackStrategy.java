package memory.cacheWriteStrategy;

import memory.Cache;
import memory.Memory;
import memory.cacheMappingStrategy.MappingStrategy;


/**
 * @Author: A cute TA
 * @CreateTime: 2020-11-12 11:39
 */
public class WriteBackStrategy extends WriteStrategy {


    @Override
    public Boolean isWriteBack() {
        return true;
    }

}
