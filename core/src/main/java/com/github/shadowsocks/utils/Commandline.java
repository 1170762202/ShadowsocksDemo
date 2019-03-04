package com.github.shadowsocks.utils;

import kotlin.TypeCastException;
import kotlin.collections.ArraysKt;
import kotlin.collections.IntIterator;
import kotlin.jvm.internal.Intrinsics;
import kotlin.ranges.RangesKt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * @author 陈志鹏
 * @date 2019/2/27
 */
public class Commandline {

    @NotNull
    public static String toString(@Nullable Iterable args) {
        if (args == null) {
            return "";
        } else {
            StringBuilder result = new StringBuilder();
            Iterator var4 = args.iterator();

            while(var4.hasNext()) {
                String arg = (String)var4.next();
                CharSequence var5 = (CharSequence)result;
                if (var5.length() > 0) {
                    result.append(' ');
                }

                Iterable $receiver$iv = (Iterable) RangesKt.until(0, arg.length());
                Collection destination$iv$iv = new ArrayList(arg.length());
                Iterator var8 = $receiver$iv.iterator();

                while(var8.hasNext()) {
                    int item$iv$iv = ((IntIterator)var8).nextInt();
                    Character var15 = arg.charAt(item$iv$iv);
                    destination$iv$iv.add(var15);
                }

                $receiver$iv = (Iterable)((List)destination$iv$iv);
                Iterator var6 = $receiver$iv.iterator();

                while(var6.hasNext()) {
                    Object element$iv = var6.next();
                    char it = (Character)element$iv;
                    switch(it) {
                        case ' ':
                        case '"':
                        case '\'':
                        case '\\':
                            result.append('\\');
                            result.append(it);
                            break;
                        default:
                            result.append(it);
                    }
                }
            }

            String var10000 = result.toString();
            return var10000;
        }
    }

    @NotNull
    public static String toString(@NotNull String[] args) {
        return toString(ArraysKt.asIterable(args));
    }

    @NotNull
    public static String[] translateCommandline(@Nullable String toProcess) {
        if (toProcess != null) {
            CharSequence var2 = (CharSequence)toProcess;
            if (var2.length() != 0) {
                int normal = 0;
                int inQuote = 1;
                int inDoubleQuote = 2;
                int state = normal;
                StringTokenizer tok = new StringTokenizer(toProcess, "\\\"' ", true);
                ArrayList result = new ArrayList();
                StringBuilder current = new StringBuilder();
                boolean lastTokenHasBeenQuoted = false;
                boolean lastTokenIsSlash = false;

                while(true) {
                    while(tok.hasMoreTokens()) {
                        String nextTok = tok.nextToken();
                        if (state == inQuote) {
                            if (Intrinsics.areEqual("'", nextTok)) {
                                lastTokenHasBeenQuoted = true;
                                state = normal;
                            } else {
                                current.append(nextTok);
                            }
                        } else if (state == inDoubleQuote) {
                            if (Intrinsics.areEqual("\"", nextTok)) {
                                if (lastTokenIsSlash) {
                                    current.append(nextTok);
                                    lastTokenIsSlash = false;
                                } else {
                                    lastTokenHasBeenQuoted = true;
                                    state = normal;
                                }
                            } else if (Intrinsics.areEqual("\\", nextTok)) {
                                boolean var10000;
                                if (lastTokenIsSlash) {
                                    current.append(nextTok);
                                    var10000 = false;
                                } else {
                                    var10000 = true;
                                }

                                lastTokenIsSlash = var10000;
                            } else {
                                if (lastTokenIsSlash) {
                                    current.append("\\");
                                    lastTokenIsSlash = false;
                                }

                                current.append(nextTok);
                            }
                        } else {
                            if (lastTokenIsSlash) {
                                current.append(nextTok);
                                lastTokenIsSlash = false;
                            } else if (Intrinsics.areEqual("\\", nextTok)) {
                                lastTokenIsSlash = true;
                            } else if (Intrinsics.areEqual("'", nextTok)) {
                                state = inQuote;
                            } else if (Intrinsics.areEqual("\"", nextTok)) {
                                state = inDoubleQuote;
                            } else if (Intrinsics.areEqual(" ", nextTok)) {
                                label95: {
                                    if (!lastTokenHasBeenQuoted) {
                                        CharSequence var13 = (CharSequence)current;
                                        if (var13.length() <= 0) {
                                            break label95;
                                        }
                                    }

                                    result.add(current.toString());
                                    current.setLength(0);
                                }
                            } else {
                                current.append(nextTok);
                            }

                            lastTokenHasBeenQuoted = false;
                        }
                    }

                    label83: {
                        if (!lastTokenHasBeenQuoted) {
                            CharSequence var15 = (CharSequence)current;
                            if (var15.length() <= 0) {
                                break label83;
                            }
                        }

                        result.add(current.toString());
                    }

                    if (state != inQuote && state != inDoubleQuote) {
                        if (lastTokenIsSlash) {
                            throw new IllegalArgumentException("escape character following nothing in " + toProcess);
                        }

                        Collection $receiver$iv = (Collection)result;
                        Object[] var17 = $receiver$iv.toArray(new String[0]);
                        if (var17 == null) {
                            throw new TypeCastException("null cannot be cast to non-null type kotlin.Array<T>");
                        }

                        return (String[])var17;
                    }

                    throw new IllegalArgumentException("unbalanced quotes in " + toProcess);
                }
            }
        }

        return new String[0];
    }
}
