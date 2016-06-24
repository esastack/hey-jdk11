/*
 * Copyright (c) 2005, 2013, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package sun.tools.jmap;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Collection;

import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;
import com.sun.tools.attach.AttachNotSupportedException;
import sun.tools.attach.HotSpotVirtualMachine;
import sun.tools.common.ProcessArgumentMatcher;

/*
 * This class is the main class for the JMap utility. It parses its arguments
 * and decides if the command should be satisfied using the VM attach mechanism
 * or an SA tool. At this time the only option that uses the VM attach mechanism
 * is the -dump option to get a heap dump of a running application. All other
 * options are mapped to SA tools.
 */
public class JMap {

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            usage(1); // no arguments
        }

        checkForUnsupportedOptions(args);

        // the chosen option
        String option = null;

        // First iterate over the options (arguments starting with -).  There should be
        // one.
        int optionCount = 0;
        while (optionCount < args.length) {
            String arg = args[optionCount];
            if (!arg.startsWith("-")) {
                break;
            }
            if (arg.equals("-help") || arg.equals("-h")) {
                usage(0);
            } else {
                if (option != null) {
                    usage(1);  // option already specified
                }
                option = arg;
            }
            optionCount++;
        }

        // if no option provided then use default.
        if (option == null) {
            usage(0);
        }

        // Next we check the parameter count.
        int paramCount = args.length - optionCount;
        if (paramCount != 1) {
            usage(1);
        }

        String pidArg = args[1];
        // Here we handle the built-in options
        // As more options are added we should create an abstract tool class and
        // have a table to map the options
        ProcessArgumentMatcher ap = new ProcessArgumentMatcher(pidArg);
        Collection<VirtualMachineDescriptor> vids = ap.getVirtualMachineDescriptors(JMap.class);

        if (vids.isEmpty()) {
            System.err.println("Could not find any processes matching : '" + pidArg + "'");
            System.exit(1);
        }

        for (VirtualMachineDescriptor vid : vids) {
            String pid = vid.id();
            if (vids.size() > 1) {
                System.out.println("Pid:" + pid);
            }
            if (option.equals("-histo")) {
                histo(pid, "");
            } else if (option.startsWith("-histo:")) {
                histo(pid, option.substring("-histo:".length()));
            } else if (option.startsWith("-dump:")) {
                dump(pid, option.substring("-dump:".length()));
            } else if (option.equals("-finalizerinfo")) {
                executeCommandForPid(pid, "jcmd", "GC.finalizer_info");
            } else if (option.equals("-clstats")) {
                executeCommandForPid(pid, "jcmd", "GC.class_stats");
            } else {
              usage(1);
            }
        }
    }

    private static void executeCommandForPid(String pid, String command, Object ... args)
        throws AttachNotSupportedException, IOException,
               UnsupportedEncodingException {
        VirtualMachine vm = VirtualMachine.attach(pid);

        // Cast to HotSpotVirtualMachine as this is an
        // implementation specific method.
        HotSpotVirtualMachine hvm = (HotSpotVirtualMachine) vm;
        try (InputStream in = hvm.executeCommand(command, args)) {
          // read to EOF and just print output
          byte b[] = new byte[256];
          int n;
          do {
              n = in.read(b);
              if (n > 0) {
                  String s = new String(b, 0, n, "UTF-8");
                  System.out.print(s);
              }
          } while (n > 0);
        }
        vm.detach();
    }

    private static void histo(String pid, String options)
        throws AttachNotSupportedException, IOException,
               UnsupportedEncodingException {
        String liveopt = "-all";
        if (options.equals("") || options.equals("all")) {
            //  pass
        }
        else if (options.equals("live")) {
            liveopt = "-live";
        }
        else {
            usage(1);
        }

        // inspectHeap is not the same as jcmd GC.class_histogram
        executeCommandForPid(pid, "inspectheap", liveopt);
    }

    private static void dump(String pid, String options)
        throws AttachNotSupportedException, IOException,
               UnsupportedEncodingException {

        String subopts[] = options.split(",");
        String filename = null;
        String liveopt = "-all";

        for (int i = 0; i < subopts.length; i++) {
            String subopt = subopts[i];
            if (subopt.equals("live")) {
                liveopt = "-live";
            } else if (subopt.startsWith("file=")) {
                // file=<file> - check that <file> is specified
                if (subopt.length() > 5) {
                    filename = subopt.substring(5);
                }
            }
        }

        if (filename == null) {
            usage(1);  // invalid options or no filename
        }

        // get the canonical path - important to avoid just passing
        // a "heap.bin" and having the dump created in the target VM
        // working directory rather than the directory where jmap
        // is executed.
        filename = new File(filename).getCanonicalPath();
        // dumpHeap is not the same as jcmd GC.heap_dump
        executeCommandForPid(pid, "dumpheap", filename, liveopt);
    }

    private static void checkForUnsupportedOptions(String[] args) {
        // Check arguments for -F, -m, and non-numeric value
        // and warn the user that SA is not supported anymore

        int paramCount = 0;

        for (String s : args) {
            if (s.equals("-F")) {
                SAOptionError("-F option used");
            }

            if (s.equals("-heap")) {
                SAOptionError("-heap option used");
            }

            /* Reimplemented using jcmd, output format is different
               from original one

            if (s.equals("-clstats")) {
                warnSA("-clstats option used");
            }

            if (s.equals("-finalizerinfo")) {
                warnSA("-finalizerinfo option used");
            }
            */

            if (! s.startsWith("-")) {
                paramCount += 1;
            }
        }

        if (paramCount > 1) {
            SAOptionError("More than one non-option argument");
        }
    }

    private static void SAOptionError(String msg) {
        System.err.println("Error: " + msg);
        System.err.println("Cannot connect to core dump or remote debug server. Use jhsdb jmap instead");
        System.exit(1);
    }

    // print usage message
    private static void usage(int exit) {
        System.err.println("Usage:");
        System.err.println("    jmap -clstats <pid>");
        System.err.println("        to connect to running process and print class loader statistics");
        System.err.println("    jmap -finalizerinfo <pid>");
        System.err.println("        to connect to running process and print information on objects awaiting finalization");
        System.err.println("    jmap -histo[:live] <pid>");
        System.err.println("        to connect to running process and print histogram of java object heap");
        System.err.println("        if the \"live\" suboption is specified, only count live objects");
        System.err.println("    jmap -dump:<dump-options> <pid>");
        System.err.println("        to connect to running process and dump java heap");
        System.err.println("");
        System.err.println("    dump-options:");
        System.err.println("      live         dump only live objects; if not specified,");
        System.err.println("                   all objects in the heap are dumped.");
        System.err.println("      format=b     binary format");
        System.err.println("      file=<file>  dump heap to <file>");
        System.err.println("");
        System.err.println("    Example: jmap -dump:live,format=b,file=heap.bin <pid>");
        System.exit(exit);
    }
}
