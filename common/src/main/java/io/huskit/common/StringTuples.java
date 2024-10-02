package io.huskit.common;

import java.util.*;

public class StringTuples {

    Set<Tuple<String, String>> args;

    public StringTuples() {
        this.args = new LinkedHashSet<>();
    }

    public StringTuples(List<? extends CharSequence> args) {
        this.args = new LinkedHashSet<>();
        var size = args.size();
        if (size == 1) {
            add(args.get(0).toString());
        } else if (size == 2) {
            add(args.get(0).toString(), args.get(1).toString());
        } else {
            for (var i = 0; i < size; i += 2) {
                if (i + 1 < size) {
                    add(args.get(i).toString(), args.get(i + 1).toString());
                } else {
                    add(args.get(i).toString());
                }
            }
        }
    }

    public StringTuples(CharSequence... args) {
        this(Arrays.asList(args));
    }

    public List<String> toList(CharSequence... additionalArgs) {
        var list = new ArrayList<String>((args.size() * 2) + additionalArgs.length);
        for (var arg : args) {
            list.add(arg.left());
            var right = arg.right();
            if (!right.isEmpty()) {
                list.add(right);
            }
        }
        for (var arg : additionalArgs) {
            var strArg = arg.toString();
            if (!strArg.isEmpty()) {
                list.add(strArg);
            }
        }
        return list;
    }

    public void add(CharSequence arg) {
        this.args.add(Tuple.of(arg.toString(), ""));
    }

    public void add(CharSequence key, CharSequence value) {
        this.args.add(Tuple.of(key.toString(), value.toString()));
    }

    public void add(CharSequence key, CharSequence value, Object... valueArgs) {
        this.args.add(Tuple.of(key.toString(), String.format(value.toString(), valueArgs)));
    }
}
