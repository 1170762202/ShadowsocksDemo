package com.github.shadowsocks.plugin;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Objects;
import java.util.StringTokenizer;

public class PluginOptions extends HashMap<String, String> {

    private String id = "";

    public PluginOptions(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    public PluginOptions(int initialCapacity) {
        super(initialCapacity);
    }

    public PluginOptions() {
    }

    private PluginOptions(String options, boolean parseId) {
        this();
        if (options == null || options.length() == 0) {
            return;
        }
        boolean allow = true;
        char[] chars = options.toCharArray();
        for (char aChar : chars) {
            if (Character.isISOControl(aChar)) {
                allow = false;
                break;
            }
        }

        String key = null;
        if (!allow) {
            key = "No control characters allowed.";
            throw (new IllegalStateException(key.toString()));
        } else {
            StringTokenizer tokenizer = new StringTokenizer(options + ';', "\\=;", true);
            StringBuilder current = new StringBuilder();

            while (tokenizer.hasMoreTokens()) {
                String nextToken = tokenizer.nextToken();
                if (nextToken == null) {
                    continue;
                }
                switch (nextToken) {
                    case "\\": {
                        current.append(tokenizer.nextToken());
                        continue;
                    }
                    case "=": {
                        if (key == null) {
                            key = current.toString();
                            current.setLength(0);
                        } else {
                            current.append(nextToken);
                        }
                        continue;
                    }
                    case ";": {
                        if (key != null) {
                            this.put(key, current.toString());
                            key = null;
                        } else {
                            if (current.length() > 0) {
                                if (parseId) {
                                    this.id = current.toString();
                                } else {
                                    this.put(current.toString(), null);
                                }
                            }
                        }

                        current.setLength(0);
                        parseId = false;
                        continue;
                    }
                    default: {
                        current.append(nextToken);
                    }
                }
            }
        }
    }

    public PluginOptions(@Nullable String options) {
        this(options, true);
    }

    public PluginOptions(@NotNull String id, @Nullable String options) {
        this(options, false);
        this.id = id;
    }

    public String putWithDefault(@NotNull String key, @Nullable String value, @Nullable String var3) {
        return (value == null || value == var3) ? remove(key) : put(key, value);
    }

    private void append(StringBuilder result, String str) {
        for (int i = 0; i < str.length(); i++) {
            char charAt = str.charAt(i);
            switch (charAt) {
                case ';':
                case '=':
                case '\\':
                    result.append('\\');
                    result.append(charAt);
                    break;
                default:
                    result.append(charAt);
            }
        }
    }

    public String toString(boolean trimId) {
        StringBuilder result = new StringBuilder();
        if (!trimId) {
            if (this.id == null || this.id.length() == 0) {
                return "";
            }
            this.append(result, this.id);
        }

        Iterator iterator = this.entrySet().iterator();
        while (iterator.hasNext()) {
            Entry entry = (Entry) iterator.next();
            String value = (String) entry.getValue();
            if (result.length() > 0) {
                result.append(';');
            }
            if (value != null) {
                result.append('=');
                this.append(result, value);
            }
        }
        return result.toString();
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return toString(true);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else {
            return (getClass() == ((o != null) ? o.getClass() : null)) && super.equals(o) && id == ((PluginOptions) o).id;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(new Object[]{super.hashCode(), this.id});
    }
}
