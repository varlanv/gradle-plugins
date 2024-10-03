package io.huskit.common.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Reader;

public class TeeBufferedReader extends BufferedReader {

    PrintStream printStream;

    public TeeBufferedReader(Reader in, PrintStream printStream) {
        super(in);
        this.printStream = printStream;
    }

    public TeeBufferedReader(Reader in) {
        this(in, System.out);
    }

    @Override
    public String readLine() throws IOException {
        var line = super.readLine();
        printStream.println(line);
        return line;
    }
}
