package com.github.shadowsocks.acl;

import android.content.Context;
import android.support.v7.util.SortedList;
import android.text.TextUtils;

import com.github.shadowsocks.Core;
import com.github.shadowsocks.preference.DataStore;
import com.github.shadowsocks.utils.IteratorUtils;
import com.github.shadowsocks.utils.Subnet;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import kotlin.collections.ArraysKt;
import kotlin.collections.CollectionsKt;
import kotlin.io.FilesKt;
import kotlin.io.TextStreamsKt;
import kotlin.jvm.internal.Intrinsics;
import kotlin.sequences.Sequence;
import kotlin.sequences.SequencesKt;
import kotlin.text.Charsets;
import kotlin.text.MatchResult;
import kotlin.text.Regex;

/**
 * @author 陈志鹏
 * @date 2019/2/25
 */
public class Acl {
    @NotNull
    public static final String TAG = "Acl";
    @NotNull
    public static final String ALL = "all";
    @NotNull
    public static final String BYPASS_LAN = "bypass-lan";
    @NotNull
    public static final String BYPASS_CHN = "bypass-china";
    @NotNull
    public static final String BYPASS_LAN_CHN = "bypass-lan-china";
    @NotNull
    public static final String GFWLIST = "gfwlist";
    @NotNull
    public static final String CHINALIST = "china-list";
    @NotNull
    public static final String CUSTOM_RULES = "custom-rules";
    @NotNull
    private static final Regex networkAclParser;
    @NotNull
    private final SortedList bypassHostnames;
    @NotNull
    private final SortedList proxyHostnames;
    @NotNull
    private final SortedList subnets;
    @NotNull
    private final SortedList urls;
    private boolean bypass = false;

    static {
        String var0 = "^IMPORT_URL\\s*<(.+)>\\s*$";
        networkAclParser = new Regex(var0);
    }

    public boolean getBypass(){
       return bypass;
    }

    public void setBypass(boolean bypass){
        this.bypass = bypass;
    }

    public Acl() {
        this.bypassHostnames = new SortedList(String.class, Acl.StringSorter.INSTANCE);
        this.proxyHostnames = new SortedList(String.class, Acl.StringSorter.INSTANCE);
        this.subnets = new SortedList(Subnet.class, Acl.SubnetSorter.INSTANCE);
        this.urls = new SortedList(URL.class, Acl.URLSorter.INSTANCE);
    }

    public static File getFile(String id) {
        return getFile(id, Core.getDeviceStorage());
    }

    public static File getFile(String id, Context context) {
        return new File(context.getNoBackupFilesDir(), id + ".acl");
    }

    public static Acl getCustomRules() {
        Acl acl = new Acl();
        String str = DataStore.publicStore.getString(CUSTOM_RULES);
        if (str != null) {
            StringReader var5 = new StringReader(str);
            acl.fromReader((Reader) var5, true);
        }

        if (!acl.bypass) {
            acl.bypass = true;
            acl.getSubnets().clear();
        }

        return acl;
    }

    @NotNull
    public SortedList getSubnets() {
        return subnets;
    }

    public static void setCustomRules(@NotNull Acl value) {
        DataStore.publicStore.putString(CUSTOM_RULES, (!value.getBypass() || value.getSubnets().size() == 0) && value.getBypassHostnames().size() == 0 && value.getProxyHostnames().size() == 0 && value.getUrls().size() == 0 ? null : value.toString());
    }

    @NotNull
    public SortedList getBypassHostnames() {
        return bypassHostnames;
    }

    @NotNull
    public SortedList getProxyHostnames() {
        return proxyHostnames;
    }

    @NotNull
    public SortedList getUrls() {
        return urls;
    }

    public static final void save(@NotNull String id, @NotNull Acl acl) {
        FilesKt.writeText(getFile(id), acl.toString(), Charsets.UTF_8);
    }

    public static abstract class BaseSorter<T> extends SortedList.Callback<T> {
        @Override
        public void onInserted(int position, int count) {
        }

        @Override
        public boolean areContentsTheSame(@Nullable T oldItem, @Nullable T newItem) {
            return Intrinsics.areEqual(oldItem, newItem);
        }

        @Override
        public void onMoved(int fromPosition, int toPosition) {
        }

        @Override
        public void onChanged(int position, int count) {
        }

        @Override
        public void onRemoved(int position, int count) {
        }

        @Override
        public boolean areItemsTheSame(@Nullable T item1, @Nullable T item2) {
            return Intrinsics.areEqual(item1, item2);
        }

        @Override
        public int compare(@Nullable T o1, @Nullable T o2) {
            return o1 == null ? (o2 == null ? 0 : 1) : (o2 == null ? -1 : this.compareNonNull(o1, o2));
        }

        public abstract int compareNonNull(T var1, T var2);
    }

    private static class DefaultSorter<T extends Comparable<T>> extends BaseSorter<T> {

        @Override
        public int compareNonNull(T var1, T var2) {
            return var1.compareTo(var2);
        }
    }

    private static class StringSorter extends Acl.DefaultSorter {
        public static final Acl.StringSorter INSTANCE;

        static {
            INSTANCE = new Acl.StringSorter();
        }
    }

    private static final class SubnetSorter extends Acl.DefaultSorter {
        public static final Acl.SubnetSorter INSTANCE;

        static {
            INSTANCE = new Acl.SubnetSorter();
        }
    }

    private static final class URLSorter extends Acl.BaseSorter<URL> {
        private static final Comparator<URL> ordering;
        public static final URLSorter INSTANCE;

        @Override
        public int compareNonNull(@NotNull URL o1, @NotNull URL o2) {
            return ordering.compare(o1, o2);
        }

        static {
            INSTANCE = new URLSorter();
            ordering = new Comparator<URL>() {
                @Override
                public int compare(URL o1, URL o2) {
                    if (o1.getHost().equals(o2.getHost())) {
                        if (o1.getPort() == o2.getPort()) {
                            if (o1.getFile().equals(o2.getFile())) {
                                return o1.getProtocol().compareTo(o2.getProtocol());
                            } else {
                                return o1.getFile().compareTo(o2.getFile());
                            }
                        } else {
                            return Integer.valueOf(o1.getPort()).compareTo(Integer.valueOf(o2.getPort()));
                        }
                    } else {
                        return o1.getHost().compareTo(o2.getHost());
                    }
                }
            };
        }
    }

    public Acl fromAcl(Acl other) {
        this.bypassHostnames.clear();
        Iterator var3 = IteratorUtils.asIterable(other.bypassHostnames);

        while (var3.hasNext()) {
            this.bypassHostnames.add(var3.next());
        }

        this.proxyHostnames.clear();
        var3 = IteratorUtils.asIterable(other.proxyHostnames);

        while (var3.hasNext()) {
            this.proxyHostnames.add(var3.next());
        }

        this.subnets.clear();
        var3 = IteratorUtils.asIterable(other.subnets);

        while (var3.hasNext()) {
            this.subnets.add(var3.next());
        }

        this.urls.clear();
        var3 = IteratorUtils.asIterable(other.urls);

        while (var3.hasNext()) {
            this.urls.add(var3.next());
        }

        this.bypass = other.bypass;
        return this;
    }

    public final Acl fromReader(Reader reader) {
        return fromReader(reader, false);
    }

    @NotNull
    public final Acl fromReader(@NotNull Reader reader, boolean defaultBypass) {
        bypassHostnames.clear();
        proxyHostnames.clear();
        subnets.clear();
        urls.clear();
        bypass = defaultBypass;
        SortedList bypassSubnets = new SortedList(Subnet.class, SubnetSorter.INSTANCE);
        SortedList proxySubnets = new SortedList(Subnet.class, SubnetSorter.INSTANCE);
        SortedList<String> hostnames = defaultBypass ? proxyHostnames : bypassHostnames;
        SortedList<Subnet> subnets = defaultBypass ? proxySubnets : bypassSubnets;

        BufferedReader bufferedReader;
        if (reader instanceof BufferedReader) {
            bufferedReader = (BufferedReader) reader;
        } else {
            bufferedReader = new BufferedReader(reader, 8192);
        }
        Sequence it = TextStreamsKt.lineSequence(bufferedReader);
        Iterator iterator = it.iterator();
        while (true) {
            if (!iterator.hasNext()) {
                break;
            }
            String line = (String) iterator.next();
            String[] blocks = line.split("#", 2);
            String url;
            if (1 <= ArraysKt.getLastIndex(blocks)) {
                url = blocks[1];
            } else {
                url = "";
            }
            MatchResult var39 = networkAclParser.matchEntire(url);
            if (var39 != null) {
                List var40 = var39.getGroupValues();
                if (var40 != null) {
                    url = (String) CollectionsKt.getOrNull(var40, 1);
                } else {
                    url = null;
                }
            } else {
                url = null;
            }
            if (url != null) {
                try {
                    urls.add(new URL(url));
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            }
            String input = blocks[0].trim();
            switch (input) {
                case "[outbound_block_list]":
                    hostnames = null;
                    subnets = null;
                    break;
                case "[black_list]":
                case "[bypass_list]":
                    hostnames = bypassHostnames;
                    subnets = bypassSubnets;
                    break;
                case "[white_list]":
                case "[proxy_list]":
                    hostnames = proxyHostnames;
                    subnets = proxySubnets;
                    break;
                case "[reject_all]":
                case "[bypass_all]":
                    bypass = true;
                    break;
                case "[accept_all]":
                case "[proxy_all]":
                    bypass = false;
                    break;
                default:
                    if (subnets != null && !TextUtils.isEmpty(input)) {
                        Subnet subnet = Subnet.fromString(input);
                        if (subnet == null) {
                            if (hostnames != null) {
                                hostnames.add(input);
                            }
                        } else {
                            if (subnets != null) {
                                subnets.add(subnet);
                            }
                        }
                    }
                    break;
            }
        }
        SortedList list;
        if (this.bypass) {
            list = proxySubnets;
        } else {
            list = bypassSubnets;
        }
        Iterator var33 = IteratorUtils.asIterable(list);

        while (var33.hasNext()) {
            Subnet item = (Subnet) var33.next();
            this.subnets.add(item);
        }
        return this;
    }

    @NotNull
    public final Acl fromId(@NotNull String id) {
        Acl var2;
        try {
            File file = getFile(id);
            InputStream var7 = (InputStream) (new FileInputStream(file));
            Reader var6 = (Reader) (new InputStreamReader(var7, Charsets.UTF_8));
            BufferedReader var9 = var6 instanceof BufferedReader ? (BufferedReader) var6 : new BufferedReader(var6, 8192);
            var2 = fromReader(var9);
        } catch (IOException var10) {
            var2 = this;
        }

        return var2;
    }
    @NotNull
    public final Acl flatten(int depth) {
       if (depth > 0){
            for (int i = 0; i < urls.size(); i++) {
                Acl child = new Acl();
                try {
                    child.fromReader(new BufferedReader(new InputStreamReader(new FileInputStream(((URL)urls.get(i)).getFile())),8192),bypass).flatten(depth -1);
                }catch (IOException e){
                    e.printStackTrace();
                    continue;
                }
                if (bypass != child.bypass){
                    child.subnets.clear();
                    child.bypass = bypass;
                }
                for (int j = 0; j < child.bypassHostnames.size(); j++) {
                    bypassHostnames.add(child.bypassHostnames.get(j));
                }
                for (int j = 0; j < child.proxyHostnames.size(); j++) {
                    proxyHostnames.add(child.bypassHostnames.get(j));
                }
                for (int j = 0; j < child.subnets.size(); j++) {
                    subnets.add(child.subnets.get(j));
                }
            }
        }
        urls.clear();
        return this;
    }

    @Override
    @NotNull
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append(this.bypass ? "[bypass_all]\n" : "[proxy_all]\n");
        List bypassList = SequencesKt.toList(this.bypass ? CollectionsKt.asSequence((Iterable<?>) IteratorUtils.asIterable(this.bypassHostnames)) :
                SequencesKt.plus(SequencesKt.map(CollectionsKt.asSequence((Iterable<?>) IteratorUtils.asIterable(this.subnets)),null), CollectionsKt.asSequence((Iterable<?>) IteratorUtils.asIterable(this.bypassHostnames))));
        List proxyList = SequencesKt.toList(this.bypass ? SequencesKt.plus(SequencesKt.map(CollectionsKt.asSequence((Iterable<?>) IteratorUtils.asIterable(this.subnets)), null), CollectionsKt.asSequence((Iterable<?>) IteratorUtils.asIterable(this.proxyHostnames))) : CollectionsKt.asSequence((Iterable<?>) IteratorUtils.asIterable(this.proxyHostnames)));
        if (!bypassList.isEmpty()) {
            result.append("[bypass_list]\n");
            result.append(CollectionsKt.joinToString(bypassList, "\n", "","", -1,"...", null));
            result.append('\n');
        }

        if (!proxyList.isEmpty()) {
            result.append("[proxy_list]\n");
            result.append(CollectionsKt.joinToString((Iterable)proxyList, (CharSequence)"\n","","", -1,"...", null));
            result.append('\n');
        }

        result.append(CollectionsKt.joinToString((Iterable<?>) IteratorUtils.asIterable(this.urls), (CharSequence)"", (CharSequence)null, (CharSequence)null, 0, (CharSequence)null, null));
        return result.toString();
    }
}
