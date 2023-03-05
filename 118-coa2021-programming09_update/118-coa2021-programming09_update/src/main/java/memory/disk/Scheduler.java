package memory.disk;

import java.util.ArrayList;
import java.util.Arrays;

public class Scheduler {

    /**
     * 先来先服务算法
     * @param start 磁头初始位置
     * @param request 请求访问的磁道号
     * @return 平均寻道长度
     */
    public double FCFS(int start, int[] request) {
        // TODO
        if (request == null||request.length == 0) return 0;
        double time = 0;
        for (int j : request) {
            time += Math.abs(j - start);
            start = j;
        }
        time = time / request.length;
        return time;
    }

    /**
     * 最短寻道时间优先算法
     * @param start 磁头初始位置
     * @param request 请求访问的磁道号
     * @return 平均寻道长度
     */
    public double SSTF(int start, int[] request) {
        // TODO
        if (request == null||request.length == 0) return 0;
        double time = 0;
        ArrayList<Integer> list = new ArrayList<>();
        for (int i :request) list.add(i);
        while (list.size() > 0) {
            int min = minDistance(list, start);
            time += Math.abs(start - list.get(min));
            start = list.get(min);
            list.remove(min);
        }
        time = time / request.length;
        return time;
    }

    // 返回距离最小的下标
    public int minDistance (ArrayList<Integer> list, int start){
        int min = 0;
        for (int i = 1; i < list.size(); i++){
            if (Math.abs(start - list.get(i)) < Math.abs(start - list.get(min))) min = i;
        }
        return min;
    }

    /**
     * 扫描算法
     * @param start 磁头初始位置
     * @param request 请求访问的磁道号
     * @param direction 磁头初始移动方向，true表示磁道号增大的方向，false表示磁道号减小的方向
     * @return 平均寻道长度
     */
    public double SCAN(int start, int[] request, boolean direction) {
        // TODO
        if (request == null||request.length == 0) return 0;
        double time;
        if (direction) {
            if (minTrack(request) >= start) time = Math.abs(maxTrack(request) - start);
            else time = 255 - start + 255 - minTrack(request);
        }
        else{
            if (maxTrack(request) <= start) time = Math.abs(start - minTrack(request));
            else time = start + maxTrack(request);
        }
        time = time / request.length;
        return time;
    }

    public int maxTrack (int[] request) {
        int max = request[0];
        for (int i: request) {
            if (i > max) max = i;
        }
        return max;
    }

    public int minTrack (int[] request) {
        int min = request[0];
        for (int i: request) {
            if (i < min) min = i;
        }
        return min;
    }

}
