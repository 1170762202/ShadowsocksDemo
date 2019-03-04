package com.github.shadowsocks.plugin;

import android.util.Log;
import com.github.shadowsocks.utils.Commandline;
import kotlin.Pair;
import kotlin.TuplesKt;
import kotlin.collections.ArraysKt;
import kotlin.collections.CollectionsKt;
import kotlin.jvm.internal.Intrinsics;
import kotlin.ranges.RangesKt;
import kotlin.text.StringsKt;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * @author 陈志鹏
 * @date 2019/2/27
 */
public class PluginConfiguration {

    private static int INT_MAX_POWER_OF_TWO = Integer.MAX_VALUE / 2 + 1;
    @NotNull
    private Map pluginsOptions;
    @NotNull
    private String selected;

    public PluginConfiguration(List<PluginOptions> plugins) {
        super();
        this.pluginsOptions = init(plugins);
        this.selected = plugins.isEmpty() ? "" : (plugins.get(0)).getId();
    }

    private Map init(List<PluginOptions> plugins){
        ArrayList list = new ArrayList();
        Iterator var5 = plugins.iterator();

        while (var5.hasNext()) {
            PluginOptions it = (PluginOptions) var5.next();
            String var10000 = it.getId();
            Intrinsics.checkExpressionValueIsNotNull(var10000, "it.id");
            CharSequence var9 = (CharSequence) var10000;
            if (var9.length() > 0) {
                list.add(it);
            }
        }
        int capacity$iv = RangesKt.coerceAtLeast(mapCapacity(list.size()), 16);
        Map destination$iv$iv = (Map) (new LinkedHashMap(capacity$iv));
        Iterator var17 = list.iterator();

        while (var17.hasNext()) {
            PluginOptions it = (PluginOptions) var17.next();
            Pair var11 = TuplesKt.to(it.getId(), it);
            destination$iv$iv.put(var11.getFirst(), var11.getSecond());
        }

        return destination$iv$iv;
    }

    public PluginConfiguration(@NotNull Map pluginsOptions, @NotNull String selected) {
        super();
        this.pluginsOptions = pluginsOptions;
        this.selected = selected;
    }

    public PluginConfiguration(@NotNull String plugin) {
        Iterable $receiver$iv = StringsKt.split(plugin, new char[]{'\n'}, false,  6);
        ArrayList destination$iv$iv = new ArrayList(((List) $receiver$iv).size());
        Iterator var5 = $receiver$iv.iterator();

        while(var5.hasNext()) {
            String line = (String)var5.next();
            PluginOptions var10000;
            if (StringsKt.startsWith(line, "kcptun ", false)){
                PluginOptions opt = new PluginOptions();
                opt.setId("kcptun");

                try {
                    String[] var22 = Commandline.translateCommandline(line);
                    Iterator iterator = ArraysKt.drop(var22, 1).iterator();

                    while(iterator.hasNext()) {
                        String option = (String)iterator.next();
                        Map var12;
                        String var13;
                        Object var14;
                        if (Intrinsics.areEqual(option, "--nocomp")) {
                            var12 = (Map)opt;
                            var13 = "nocomp";
                            var14 = null;
                            var12.put(var13, var14);
                        } else {
                            if (!StringsKt.startsWith(option, "--", false)) {
                                throw new IllegalArgumentException("Unknown kcptun parameter: " + option);
                            }

                            var12 = opt;
                            byte var21 = 2;
                            String var23 = option.substring(var21);
                            var13 = var23;
                            var14 = iterator.next();
                            var12.put(var13, var14);
                        }
                    }
                } catch (Exception var20) {
                    Log.e("PluginConfiguration", var20.getMessage());
                }

                var10000 = opt;
            } else {
                var10000 = new PluginOptions(line);
            }

            PluginOptions var19 = var10000;
            destination$iv$iv.add(var19);
        }

        List<PluginOptions> var18 = destination$iv$iv;

        this.pluginsOptions = init(var18);
        this.selected = var18.isEmpty() ? "" : (var18.get(0)).getId();
    }

    private int  mapCapacity(int expectedSize) {
        if (expectedSize < 3) {
            return expectedSize + 1;
        }
        if (expectedSize < INT_MAX_POWER_OF_TWO) {
            return expectedSize + expectedSize / 3;
        }
        return Integer.MAX_VALUE; // any large value
    }

    @NotNull
    public final PluginOptions getOptions(@NotNull String id) {
        if (id.isEmpty()){
            return new PluginOptions();
        }else{
            PluginOptions var10000 = (PluginOptions) this.pluginsOptions.get(id);
            if (var10000 == null) {
                Plugin var10003 = PluginManager.fetchPlugins().get(id);
                var10000 = new PluginOptions(id, var10003 != null ? var10003.getDefaultConfig() : null);
            }
            return var10000;
        }
    }

    @NotNull
    public final PluginOptions getSelectedOptions() {
        return this.getOptions(this.selected);
    }

    @Override
    @NotNull
    public String toString() {
        LinkedList<PluginOptions> result = new LinkedList<PluginOptions>();
        Map var4 = this.pluginsOptions;
        Iterator var3 = var4.entrySet().iterator();

        while(var3.hasNext()) {
            Map.Entry var2 = (Map.Entry)var3.next();
            String id = (String)var2.getKey();
            PluginOptions opt = (PluginOptions)var2.getValue();
            if (Intrinsics.areEqual(id, this.selected)) {
                result.addFirst(opt);
            } else {
                result.addLast(opt);
            }
        }

        Map var7 = this.pluginsOptions;
        String var8 = this.selected;
        if (!var7.containsKey(var8)) {
            result.addFirst(this.getSelectedOptions());
        }

        return CollectionsKt.joinToString((Iterable)result, (CharSequence)"\n", (CharSequence)null, (CharSequence)null, 0, (CharSequence)null,null);
    }

    @NotNull
    public String getSelected() {
        return selected;
    }
}
