package memory.cache;

import memory.Memory;
import memory.cache.cacheReplacementStrategy.ReplacementStrategy;
import util.Transformer;

import java.util.Arrays;
import java.util.Date;

/**
 * 高速缓存抽象类
 */
public class Cache {

    public static final boolean isAvailable = true; // 默认启用Cache

    public static final int CACHE_SIZE_B = 1024 * 1024; // 1 MB 总大小

    public static final int LINE_SIZE_B = 1024; // 1 KB 行大小

    private final CacheLinePool cache = new CacheLinePool(CACHE_SIZE_B / LINE_SIZE_B);  // 总大小1MB / 行大小1KB = 1024个行

    private int SETS;   // 组数

    private int setSize;    // 每组行数

    // 单例模式
    private static final Cache cacheInstance = new Cache();

    private Cache() {
    }

    public static Cache getCache() {
        return cacheInstance;
    }
    // 获取有效位
    public boolean isValid(int rowNO){
        CacheLine cache = cacheInstance.cache.get(rowNO);
        return cache.validBit;
    }

    // 获取脏位
    public boolean isDirty(int rowNO){
        CacheLine cache = cacheInstance.cache.get(rowNO);
        return cache.dirty;
    }

    // 增加访问次数
    public void addVisited(int rowNO){
        CacheLine cache = cacheInstance.cache.get(rowNO);
        cache.visited++;
    }

    // 重置访问次数
    public void setVisited(int rowNO){
        CacheLine cache = cacheInstance.cache.get(rowNO);
        cache.visited = 1;
    }

    // 获取访问次数
    public int getVisited(int rowNO){
        CacheLine cache = cacheInstance.cache.get(rowNO);
        return cache.visited;
    }

    // 重置时间戳
    public void setTimeStamp(int rowNO){
        CacheLine cache = cacheInstance.cache.get(rowNO);
        cache.timeStamp = 0L;
    }

    //
    public void setTimeStamp1(int rowNO){
        CacheLine cache = cacheInstance.cache.get(rowNO);
        cache.timeStamp = 1L;
    }

    //
    public void changeTimeStamp (int rowNO) {
        CacheLine cache = cacheInstance.cache.get(rowNO);
        cache.timeStamp = new Date().getTime();
    }

    // 获取时间戳
    public long getTimeStamp(int rowNO){
        CacheLine cache = cacheInstance.cache.get(rowNO);
        return cache.timeStamp;
    }

    // 增加时间戳
    public void addTimeStamp (int rowNO){
        CacheLine cache = cacheInstance.cache.get(rowNO);
        cache.timeStamp++;
    }

    // 获取该行数据
    public char[] getData(int rowNO){
        CacheLine cache = cacheInstance.cache.get(rowNO);
        return cache.getData();
    }

    // 获取该行Tag
    public char[] getTag (int rowNO){
        CacheLine cache = cacheInstance.cache.get(rowNO);
        return cache.getTag();
    }

    public int getSETS () {
        return this.SETS;
    }
    public int getSetSize () {
        return this.setSize;
    }

    private ReplacementStrategy replacementStrategy;    // 替换策略

    public static boolean isWriteBack;   // 写策略

    private final Transformer transformer = new Transformer();


    /**
     * 读取[pAddr, pAddr + len)范围内的连续数据，可能包含多个数据块的内容
     *
     * @param pAddr 数据起始点(32位物理地址 = 22位块号 + 10位块内地址)
     * @param len   待读数据的字节数
     * @return 读取出的数据，以char数组的形式返回
     */
    public char[] read(String pAddr, int len) {
        char[] data = new char[len];
        int addr = Integer.parseInt(transformer.binaryToInt("0" + pAddr));
        int upperBound = addr + len;
        int index = 0;
        while (addr < upperBound) {
            int nextSegLen = LINE_SIZE_B - (addr % LINE_SIZE_B);
            if (addr + nextSegLen >= upperBound) {
                nextSegLen = upperBound - addr;
            }
            int rowNO = fetch(transformer.intToBinary(String.valueOf(addr)));
            char[] cache_data = cache.get(rowNO).getData();
            int i = 0;
            while (i < nextSegLen) {
                data[index] = cache_data[addr % LINE_SIZE_B + i];
                index++;
                i++;
            }
            addr += nextSegLen;
        }
        return data;
    }

    /**
     * 向cache中写入[pAddr, pAddr + len)范围内的连续数据，可能包含多个数据块的内容
     *
     * @param pAddr 数据起始点(32位物理地址 = 22位块号 + 10位块内地址)
     * @param len   待写数据的字节数
     * @param data  待写数据
     */
    public void write(String pAddr, int len, char[] data) {
        int addr = Integer.parseInt(transformer.binaryToInt("0" + pAddr));
        int upperBound = addr + len;
        int index = 0;
        while (addr < upperBound) {
            int nextSegLen = LINE_SIZE_B - (addr % LINE_SIZE_B);
            if (addr + nextSegLen >= upperBound) {
                nextSegLen = upperBound - addr;
            }
            int rowNO = fetch(transformer.intToBinary(String.valueOf(addr)));
            char[] cache_data = cache.get(rowNO).getData();
            int i = 0;
            while (i < nextSegLen) { // 更新cache里面的数据
                cache_data[addr % LINE_SIZE_B + i] = data[index];
                index++;
                i++;
            }

            // TODO
            // 更新cache里面的数据后也要同步更新主存内的数据
            // cache_data: 已经更新后的cache数据
            if (!isWriteBack){ // 写直达
                Memory.getMemory().write(pAddr,len,data);
            }
            CacheLine theCache = cache.get(rowNO);
            theCache.dirty = true;

            addr += nextSegLen;
            int z = 0;
        }
    }

    /**
     * 查询{@link Cache#cache}表以确认包含pAddr的数据块是否在cache内
     * 如果目标数据块不在Cache内，则将其从内存加载到Cache
     *
     * @param pAddr 数据起始点(32位物理地址 = 22位块号 + 10位块内地址)
     * @return 数据块在Cache中的对应行号
     */
    private int fetch(String pAddr) {
        // TODO
        //计算数据在主存中的块号
        int blockNO = getBlockNO(pAddr);
        int lineNumber = map(blockNO); //调用map得在cache中的行号

        if (lineNumber == -1) { //当目标数据块不在cache内，需更新
            if (this.SETS == 1024 && this.setSize == 1) {
                lineNumber = blockNO % 1024;  //直接映射
            }
            else if  (SETS == 1 && setSize == 1024) { // 全相联映射
                lineNumber = blockNO;
            }
            else { // 组相联映射
                lineNumber = blockNO % this.SETS;
                lineNumber = (lineNumber) * this.setSize;
            }

            //取pAddr前22位得到块号(此块号也为应更新后的块号)，后补零得到32位，根据块号到内存中取得相应的数据，该数据即为CacheLinePool中data，然后用update进行更新；
            char[] data = Memory.getMemory().read(pAddr.substring(0, 22) + "0000000000", LINE_SIZE_B);
            char[] tag;
            if (this.SETS == 1024 && this.setSize == 1) { //直接映射
                String x = pAddr.substring(0, 12);
                while (x.length() < 22) x = x + "0";
                tag = x.toCharArray();
            }
            else if  (SETS == 1 && setSize == 1024) { // 全相联映射
                tag = pAddr.substring(0, 22).toCharArray();
            }
            else { // 组相联映射
                String x = pAddr.substring(0, (int)(22 - Math.log(this.SETS)));
                while (x.length() < 22) x = x + "0";
                tag = x.toCharArray();
            }
            int start = (lineNumber / this.setSize) * this.setSize;
            int end = start + this.setSize;
            if (replacementStrategy != null) lineNumber = replacementStrategy.replace(start,end,tag,data);
            else {
                update(lineNumber,tag,data);
            }
            return lineNumber;

        }
        else{ //当目标数据块在cache内
            replacementStrategy.hit(lineNumber);
            return lineNumber;
        }
    }

    /**
     * 根据目标数据内存地址前22位的int表示，进行映射
     *
     * @param blockNO 数据在内存中的块号
     * @return 返回cache中所对应的行，-1表示未命中
     */
    private int map(int blockNO) {
        // TODO
        if (this.SETS == 1024 && this.setSize == 1){
            int lineNumber = blockNO % 1024;  //直接映射

            //得到块号所对应的Cache行的tag
            CacheLine theCache = cache.get(lineNumber);
            if (!theCache.validBit) return -1;
            char[] tag = theCache.getTag();
            String cacheTag = "";
            for (int i = 0; i < 12; i++){
                cacheTag = cacheTag + tag[i];
            }
            //得到主存中的tag
            String memoryTag = transformer.intToBinary(""+ blockNO);
            while (memoryTag.length() < 22) memoryTag = "0" + memoryTag;
            if (memoryTag.length() == 32) memoryTag = memoryTag.substring(10);
            memoryTag = memoryTag.substring(0, 12);
            //将对应cache行中的标记和主存地址高t（22-10）位标记进行比较，若相等则有效位为1
            if (cacheTag.equals(memoryTag)) return lineNumber;
            return -1;
        }
        else if (SETS == 1 && setSize == 1024){ // 全相联映射
            //得到主存中的tag
            String memoryTag = transformer.intToBinary(""+ blockNO);
            while (memoryTag.length() < 22) memoryTag = "0" + memoryTag;
            if (memoryTag.length() == 32) memoryTag = memoryTag.substring(10);
            String cacheTag = "";
            for (int i = 0; i < 1024; i++){
                //得到块号所对应的Cache行的tag
                cacheTag = "";
                CacheLine theCache = cache.get(i);
                if (!theCache.validBit) continue;
                char[] tag = theCache.getTag();
                for (int j = 0; j < 22; j++){
                    cacheTag = cacheTag + tag[j];
                }
                //将cache中每个行都与memoryTag进行比较
                if (cacheTag.equals(memoryTag)) return i;
            }
            return -1;
        }
        else{ // 组关联映射
            int lineNumber = blockNO % this.SETS;
            lineNumber = (lineNumber) * this.setSize;
            for (int x = 0; x < this.setSize; x++){
                //得到块号所对应的Cache行的tag
                CacheLine theCache = cache.get(lineNumber);
                if (!theCache.validBit) return -1;
                char[] tag = theCache.getTag();
                String cacheTag = "";
                for (int i = 0; i < (int)(22 - Math.log(this.SETS)); i++){
                    cacheTag = cacheTag + tag[i];
                }
                //得到主存中的tag
                String memoryTag = transformer.intToBinary(""+ blockNO);
                while (memoryTag.length() < 22) memoryTag = "0" + memoryTag;
                if (memoryTag.length() == 32) memoryTag = memoryTag.substring(10);
                memoryTag = memoryTag.substring(0, (int)(22 - Math.log(this.SETS)));
                //将对应cache行中的标记和主存地址高t（22-Math.log(this.SETS)位标记进行比较，若相等则有效位为1
                if (cacheTag.equals(memoryTag)) return lineNumber;
                lineNumber++;
            }
            return -1;
        }
    }

    /**
     * 更新cache
     *
     * @param rowNO 需要更新的cache行号
     * @param tag   待更新数据的Tag
     * @param input 待更新的数据
     */
    public void update(int rowNO, char[] tag, char[] input) {
        // TODO
        CacheLine theCache = cache.get(rowNO);
        String pAddr = String.valueOf(theCache.getTag());
        int x = rowNO/this.setSize;
        int p_n = Integer.parseInt(transformer.binaryToInt("0" + pAddr)) +x;
        pAddr = transformer.intToBinary(Integer.toString(p_n));
        pAddr = pAddr.substring(10) + "0000000000";
        if (isWriteBack) {
            if (theCache.dirty){
                Memory.getMemory().write(pAddr,LINE_SIZE_B,theCache.data);
                theCache.dirty = false;
            }
        }
        theCache.data = input;
        theCache.tag = tag;
        theCache.validBit = true;


    }

    /**
     * 从32位物理地址(22位块号 + 10位块内地址)获取目标数据在内存中对应的块号
     *
     * @param pAddr 32位物理地址
     * @return 数据在内存中的块号
     */
    private int getBlockNO(String pAddr) {
        return Integer.parseInt(transformer.binaryToInt("0" + pAddr.substring(0, 22)));
    }


    /**
     * 该方法会被用于测试，请勿修改
     * 使用策略模式，设置cache的替换策略
     *
     * @param replacementStrategy 替换策略
     */
    public void setReplacementStrategy(ReplacementStrategy replacementStrategy) {
        this.replacementStrategy = replacementStrategy;
    }

    /**
     * 该方法会被用于测试，请勿修改
     *
     * @param SETS 组数
     */
    public void setSETS(int SETS) {
        this.SETS = SETS;
    }

    /**
     * 该方法会被用于测试，请勿修改
     *
     * @param setSize 每组行数
     */
    public void setSetSize(int setSize) {
        this.setSize = setSize;
    }

    /**
     * 告知Cache某个连续地址范围内的数据发生了修改，缓存失效
     * 该方法仅在memory类中使用，请勿修改
     *
     * @param pAddr 发生变化的数据段的起始地址
     * @param len   数据段长度
     */
    public void invalid(String pAddr, int len) {
        int from = getBlockNO(pAddr);
        Transformer t = new Transformer();
        int to = getBlockNO(t.intToBinary(String.valueOf(Integer.parseInt(t.binaryToInt("0" + pAddr)) + len - 1)));

        for (int blockNO = from; blockNO <= to; blockNO++) {
            int rowNO = map(blockNO);
            if (rowNO != -1) {
                cache.get(rowNO).validBit = false;
            }
        }
    }

    /**
     * 清除Cache全部缓存
     * 该方法会被用于测试，请勿修改
     */
    public void clear() {
        for (CacheLine line : cache.clPool) {
            if (line != null) {
                line.validBit = false;
                line.dirty = false;
            }
        }
    }

    /**
     * 输入行号和对应的预期值，判断Cache当前状态是否符合预期
     * 这个方法仅用于测试，请勿修改
     *
     * @param lineNOs     行号
     * @param validations 有效值
     * @param tags        tag
     * @return 判断结果
     */
    public boolean checkStatus(int[] lineNOs, boolean[] validations, char[][] tags) {
        if (lineNOs.length != validations.length || validations.length != tags.length) {
            return false;
        }
        for (int i = 0; i < lineNOs.length; i++) {
            CacheLine line = cache.get(lineNOs[i]);
            if (line.validBit != validations[i]) {
                return false;
            }
            if (!Arrays.equals(line.getTag(), tags[i])) {
                return false;
            }
        }
        return true;
    }


    /**
     * 负责对CacheLine进行动态初始化
     */
    private static class CacheLinePool {

        private final CacheLine[] clPool;

        /**
         * @param lines Cache的总行数
         */
        CacheLinePool(int lines) {
            clPool = new CacheLine[lines];
        }

        private CacheLine get(int rowNO) {
            CacheLine l = clPool[rowNO];
            if (l == null) {
                clPool[rowNO] = new CacheLine();
                l = clPool[rowNO];
            }
            return l;
        }
    }


    /**
     * Cache行，每行长度为(1+22+{@link Cache#LINE_SIZE_B})
     */
    private static class CacheLine {

        // 有效位，标记该条数据是否有效
        boolean validBit = false;

        // 脏位，标记该条数据是否被修改
        boolean dirty = false;

        // 用于LFU算法，记录该条cache使用次数
        int visited = 0;

        // 用于LRU和FIFO算法，记录该条数据时间戳
        Long timeStamp = 0L;

        // 标记，占位长度为22位，有效长度取决于映射策略：
        // 直接映射: 12 位
        // 全关联映射: 22 位
        // (2^n)-路组关联映射: 22-(10-n) 位
        // 注意，tag在物理地址中用高位表示，如：直接映射(32位)=tag(12位)+行号(10位)+块内地址(10位)，
        // 那么对于值为0b1111的tag应该表示为0000000011110000000000，其中前12位为有效长度
        char[] tag = new char[22];

        // 数据
        char[] data = new char[LINE_SIZE_B];

        char[] getData() {
            return this.data;
        }

        char[] getTag() {
            return this.tag;
        }

    }
}
