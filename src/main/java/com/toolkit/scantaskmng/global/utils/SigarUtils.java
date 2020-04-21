package com.toolkit.scantaskmng.global.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.toolkit.scantaskmng.global.redis.config.RedisCacheConfiguration;
import org.hyperic.sigar.*;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class SigarUtils {
    private static Sigar sigar;

    public static Sigar getSigar() {
        if (sigar == null) {
            sigar = new Sigar();
        }
        return sigar;
    }

    public static CpuInfo[] getCpuInfos() {
        CpuInfo[] cpuInfos = new CpuInfo[0];
        try {
            cpuInfos = getSigar().getCpuInfoList();
        } catch (SigarException e) {
            e.printStackTrace();
        }

        return cpuInfos;
    }

    public static CpuPerc[] getCpuPercent() {
        CpuPerc[] cpuPercs = new CpuPerc[0];
        try {
            cpuPercs = getSigar().getCpuPercList();
        } catch (SigarException e) {
            e.printStackTrace();
        }

        return cpuPercs;
    }

    public static double getCpuTotalPercent(CpuPerc[] cpuPercs) {
        double sum = 0.0;
        for (CpuPerc cpuPerc : cpuPercs) {
            sum += cpuPerc.getCombined();
        }
        return sum / cpuPercs.length;
    }

    public static Mem getMemInfos() {
        Mem memory = null;
        try {
            memory = getSigar().getMem();
        } catch (SigarException e) {
            e.printStackTrace();
        }

        return memory;
    }

    public static Swap getSwapInfos() {
        Swap swap = null;
        try {
            swap = getSigar().getSwap();
        } catch (SigarException e) {
            e.printStackTrace();
        }

        return swap;
    }

    public static Who[] getWhoInfos() {
        Who[] whos = new Who[0];
        try {
            whos = getSigar().getWhoList();
        } catch (SigarException e) {
            e.printStackTrace();
        }

        return whos;
    }

    public static JSONObject getFSTotalInfo() {
        JSONObject fsTotal = new JSONObject();

        long allTotal = 0;  //磁盘总大小
        long usedTotal = 0; //磁盘总使用大小

        try {
            FileSystem[] fileSystems = getSigar().getFileSystemList();
            JSONArray infos = (JSONArray) JSONArray.toJSON(fileSystems);
            for (Iterator it = infos.iterator(); it.hasNext(); ) {
                JSONObject fs = (JSONObject) it.next();
                FileSystemUsage usage = getSigar().getFileSystemUsage(fs.getString("dirName"));
                // TYPE_LOCAL_DISK: 2, TYPE_NETWORK: 3, TYPE_RAM_DISK(FlashDisk): 4, TYPE_CDROM: 5
                if (fs.getIntValue("type") == 2) {
                    // Unit: KBytes
                    long total = usage.getTotal();
                    allTotal += total;
                    long used = usage.getUsed();
                    usedTotal += used;
                }
            }
        } catch (SigarException e) {
            e.printStackTrace();
        }

        fsTotal.put("allTotal", allTotal);
        fsTotal.put("usedTotal", usedTotal);
        fsTotal.put("freeTotal", allTotal - usedTotal);
        Double usedPercentTotal = 100D * usedTotal / allTotal;   //总磁盘使用率
        fsTotal.put("usedPercentTotal", usedPercentTotal);
        fsTotal.put("freePercentTotal", 100 - usedPercentTotal);

        return fsTotal;

    }

    public static JSONArray getFSInfos() {
        JSONArray infos = new JSONArray();
        try {
            FileSystem[] fileSystems = getSigar().getFileSystemList();
            infos = (JSONArray) JSONArray.toJSON(fileSystems);
            for (Iterator it = infos.iterator(); it.hasNext(); ) {
                JSONObject fs = (JSONObject) it.next();
                FileSystemUsage usage = getSigar().getFileSystemUsage(fs.getString("dirName"));
                // TYPE_LOCAL_DISK: 2, TYPE_NETWORK: 3, TYPE_RAM_DISK(FlashDisk): 4, TYPE_CDROM: 5
                if (fs.getIntValue("type") == 2) {
                    // Unit: KBytes
                    fs.put("total", usage.getTotal());
                    fs.put("free", usage.getFree());
                    fs.put("avail", usage.getAvail());
                    fs.put("used", usage.getUsed());
                    double freePercent = 100D * usage.getFree() / usage.getTotal();
                    fs.put("usedPercent", 100 - freePercent);
                    fs.put("freePercent", freePercent);
                }
            }
//            for (JSONObject fs: infos)
        } catch (SigarException e) {
            e.printStackTrace();
        }

        return infos;
    }

    public static List<NetInterfaceConfig> getNetIConfig() {
        List<NetInterfaceConfig> configs = new ArrayList<>();
        try {
            String[] iFaces = getSigar().getNetInterfaceList();
            for (String iface : iFaces) {
                NetInterfaceConfig cfg = getSigar().getNetInterfaceConfig(iface);
                if (NetFlags.LOOPBACK_ADDRESS.equals(cfg.getAddress()) ||
                        (cfg.getFlags() & NetFlags.IFF_LOOPBACK) != 0 ||
                        NetFlags.NULL_HWADDR.equals(cfg.getHwaddr())) {
                    continue;
                }
                configs.add(cfg);
            }
        } catch (SigarException e) {
            e.printStackTrace();
        }

        return configs;
    }

    public static JSONObject getDomainInfos() {
        JSONObject infos = new JSONObject();
        try {
            String hostName = InetAddress.getLocalHost().getCanonicalHostName();
            infos.put("hostName", hostName);
            infos.put("FQDN", getSigar().getFQDN());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (SigarException e) {
            e.printStackTrace();
        }

        return infos;
    }

    public static JSONArray getIFStatInfos() {
        JSONArray infos = new JSONArray();
        try {
            String[] ifNames = getSigar().getNetInterfaceList();
            for (String name : ifNames) {
                // iface-stat
                NetInterfaceStat stat = getSigar().getNetInterfaceStat(name);
                JSONObject info = (JSONObject) JSONObject.toJSON(stat);

                // iface-config
                NetInterfaceConfig config = getSigar().getNetInterfaceConfig(name);
                info.put("name", name);
                info.put("address", config.getAddress());
                info.put("mask", config.getNetmask());

                infos.add(info);
            }
        } catch (SigarException e) {
            e.printStackTrace();
        }

        return infos;
    }

    public static boolean needWindowsHigherPrivlidge(String procName) {
        String winProcNames = "System,Registry,smss,csrss,wininit,services,Memory Compression,SecurityHealthService,SgrmBroker," +
                "lsass,fontdrvhost,RegSrvc,FlashHelperService,OfficeClickToRun,QQProtect,vmnetdhcp,MsMpEng,IntelCpHeciSvc," +
                "vmware-hostd,iNodeCmn,iNodeSec,iNode1x,GoogleCrashHandler,jhi_service,LMS,AppVShNotify,armsvc,UIUSrv,VRVEDP_M," +
                "GoogleCrashHandler64,Vrvsafec,dllhost,360tray,ChsIME,iNode Client,amddvr,amdow,HPCommRecovery,SearchIndexer,SearchProtocolHost," +
                "taskhostw,iNodeWlan,conhost,unsecapp,SearchFilterHost,winlogon,dwm,atiesrxx,atieclxx,WmiPrvSE,watchclient,360EntClient," +
                "ZhuDongFangYu,ctfmon,iNodeMon,iNodeImg,IntelCpHDCPSvc,SECOMN64,svchost,vmware-usbarbitrator64,vmnat,vmware-authd,OpswatModule," +
                "iNodePortal,IAStorDataMgrSvc,GoogleUpdate,igfxCUIService,vrvrf_c,wlanext,spoolsv,CxUIUSvc64,vmware,vmware-unity-helper,WUDFHost,"+
                "TGitCache,ComputerZService,CxUtilSvc,sppsvc,SppExtComObj,MicTray64,vmware-vmx,360leakfixer,audiodg,CxAudioSvc,CxUtilSvc,mysqld,"+
                "MicTray64,notepad++,ipconfig,slui";
        List<String> winProcNameList = Arrays.asList(winProcNames.split(","));
        return winProcNameList.contains(procName);
    }

    public static JSONArray getCpuUsage() {
        JSONArray usages = new JSONArray();
        int maxSize = 10;

        // 获取所有进程的ID
        long[] pids = new long[0];
        try {
            pids = getSigar().getProcList();
        } catch (SigarException e) {
            e.printStackTrace();
            return usages;
        }

        for (long pid : pids) {
            try {
                // 获取进程的CPU使用信息
                ProcState procState = getSigar().getProcState(pid);
//                System.out.println("SigarException:" + procState.getName());

                // Windows 系统中，需要高权限才能访问的进程，跳过
                if (SystemUtils.isWindows() && needWindowsHigherPrivlidge(procState.getName())) {
                    continue;
                }
//                ProcCpu procCpu = new ProcCpu();
//                procCpu.gather(getSigar(), pid);
                ProcCpu procCpu = getSigar().getProcCpu(pid);
                // 使用率占用排序，从高到低
                int index = 0;
                for (Iterator iter = usages.iterator(); iter.hasNext(); index++) {
                    JSONObject proc = (JSONObject) iter.next();
                    double procPercent = proc.getDoubleValue("percent");
                    if (procCpu.getPercent() > procPercent) {
                        break;
                    }
                }

                // 1. 列表没有填满时，添加一项使用率，位置由 index 确定
                // 2. 按照前述的排序位置插入一项使用率，位置由 index 确定
                if (index < maxSize) {
                    JSONObject jsonCpu = new JSONObject();
                    jsonCpu.put("pid", pid);
                    jsonCpu.put("name", procState.getName());
                    jsonCpu.put("percent", procCpu.getPercent());
                    usages.add(index, jsonCpu);

                    // 如果列表元素数量超过最大允许值，则删除尾部元素
                    if (usages.size() > maxSize) {
                        usages.remove(maxSize);
                    }
                }
            } catch (SigarException e) {
                e.printStackTrace();
//                System.out.println("SigarException:" + procState.getName());
                continue;
            }
        }

        return usages;
    }

    public static JSONArray getMemoryUsage() {
        JSONArray usages = new JSONArray();
        int maxSize = 10;

        // 获取所有进程的ID
        long[] pids = new long[0];
        Mem mem;
        try {
            pids = getSigar().getProcList();
            mem = getMemInfos();
        } catch (SigarException e) {
            e.printStackTrace();
            return usages;
        }

        for (long pid : pids) {
            try {
                // 获取进程的CPU使用信息
                ProcState procState = getSigar().getProcState(pid);
                // Windows 系统中，需要高权限才能访问的进程，跳过
                if (SystemUtils.isWindows() && needWindowsHigherPrivlidge(procState.getName())) {
                    continue;
                }

                ProcMem procMem = getSigar().getProcMem(pid);
                double usedPercent = procMem.getResident() * 1.0 / mem.getTotal();
                // 使用率占用排序，从高到低
                int index = 0;
                for (Iterator iter = usages.iterator(); iter.hasNext(); index++) {
                    JSONObject proc = (JSONObject) iter.next();
                    double procPercent = proc.getDoubleValue("percent");
                    if (usedPercent > procPercent) {
                        break;
                    }
                }

                // 1. 列表没有填满时，添加一项使用率，位置由 index 确定
                // 2. 按照前述的排序位置插入一项使用率，位置由 index 确定
                if (index < maxSize) {
                    JSONObject jsonMem = new JSONObject();
                    jsonMem.put("pid", pid);
                    jsonMem.put("name", procState.getName());
                    jsonMem.put("mem", procMem);
                    jsonMem.put("percent", usedPercent);
                    usages.add(index, jsonMem);

                    // 如果列表元素数量超过最大允许值，则删除尾部元素
                    if (usages.size() > maxSize) {
                        usages.remove(maxSize);
                    }
                }
            } catch (SigarException e) {
                e.printStackTrace();
                continue;
            }
        }

        return usages;
    }

    public static Object getSystemProps() {
//        getSigar().
        return System.getProperties();
    }
}
