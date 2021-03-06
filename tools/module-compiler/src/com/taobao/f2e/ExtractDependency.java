package com.taobao.f2e;

import org.apache.commons.cli.*;

import java.io.File;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Extract dependencies from code files.
 *
 * @author yiminghe@gmail.com
 */
public class ExtractDependency {
    /**
     * packages.
     */
    private Packages packages = new Packages();

    /**
     * exclude pattern for processedModules.
     */
    private Pattern excludePattern;

    /**
     * include pattern for processedModules.
     */
    private Pattern includePattern;

    /**
     * dependency output file.
     */
    private String output;

    /**
     * whether output compact module desc
     */
    private boolean compact = false;

    private HashMap<String, ArrayList<String>> dependencyCode = new HashMap<String, ArrayList<String>>();

    static String CODE_PREFIX = "/*Generated by KISSY Module Compiler*/\n" +
            "KISSY.config('modules', {\n";

    static String COMPACT_CODE_PREFIX = "/*Generated by KISSY Module Compiler*/\n" +
            "config({\n";

    static String CODE_SUFFIX = "\n});";

    public Packages getPackages() {
        return packages;
    }

    public void setExcludePattern(Pattern excludePattern) {
        this.excludePattern = excludePattern;
    }

    public void setIncludePattern(Pattern includePattern) {
        this.includePattern = includePattern;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    private boolean processSingle(String path) {
        Module m = this.getPackages().getModuleFromPath(path);

        if (!m.isValidFormat()) {
            System.err.println("Error: invalid module: " + path);
            return false;
        }

        m.completeModuleName();

        String name = m.getName();

        if (excludePattern != null &&
                excludePattern.matcher(name).matches()) {
            return true;
        }

        if (includePattern != null &&
                !includePattern.matcher(name).matches()) {
            return true;
        }

        String[] requires = m.getRequires();
        if (requires.length > 0) {
            dependencyCode.put(name, new ArrayList<String>(Arrays.asList(requires)));
        }
        return true;
    }

    /**
     * generate code list by module dependency map
     */
    private ArrayList<String> formCodeList() {
        ArrayList<String> codes = new ArrayList<String>();
        Set<String> keys = dependencyCode.keySet();
        for (String name : keys) {
            ArrayList<String> requires = dependencyCode.get(name);
            if (requires.size() > 0) {
                String allRs = "";
                for (String r : requires) {
                    allRs += ",'" + r + "'";
                }
                codes.add("'" + name + "': {requires: [" + allRs.substring(1) + "]}");
            }
        }
        return codes;
    }

    public static void commandRunnerCLI(String[] args) throws Exception {

        Options options = new Options();
        options.addOption("baseUrls", true, "baseUrls");
        options.addOption("packageUrls", true, "packageUrls");
        options.addOption("excludeReg", true, "excludeReg");
        options.addOption("includeReg", true, "includeReg");
        options.addOption("output", true, "output");
        options.addOption("v", "version", false, "version");
        options.addOption("compact", true, "compact mode");

        // create the command line parser
        CommandLineParser parser = new GnuParser();
        CommandLine line;

        // parse the command line arguments
        line = parser.parse(options, args);

        if (line.hasOption("v")) {
            System.out.println("KISSY Dependency Extractor 1.3.1");
            return;
        }

        ExtractDependency extractDependency = new ExtractDependency();

        Packages packages = extractDependency.getPackages();

        String baseUrlStr = line.getOptionValue("baseUrls");
        if (baseUrlStr != null) {
            packages.initByBaseUrls(baseUrlStr);
        }


        String packageUrlStr = line.getOptionValue("packageUrls");
        if (packageUrlStr != null) {
            packages.initByPackageUrls(packageUrlStr);
        }

        String compact = line.getOptionValue("compact");
        if (compact != null) {
            extractDependency.compact = true;
        }

        String excludeReg = line.getOptionValue("excludeReg");
        if (excludeReg != null) {
            excludeReg = excludeReg.replaceAll("\\s", "");
            extractDependency.setExcludePattern(Pattern.compile(excludeReg));
        }

        String includeReg = line.getOptionValue("includeReg");
        if (includeReg != null) {
            includeReg = includeReg.replaceAll("\\s", "");
            extractDependency.setIncludePattern(Pattern.compile(includeReg));
        }

        extractDependency.setOutput(line.getOptionValue("output"));

        extractDependency.run();

    }

    public void run() {
        long start = System.currentTimeMillis();
        boolean success = true;

        Map<String, Package> ps = packages.getPackages();

        for (String pName : ps.keySet()) {
            Package p = ps.get(pName);
            File packageFolder = new File(p.getBase());
            Collection<File> files = new ArrayList<File>();
            if (packageFolder.isDirectory()) {
                files = org.apache.commons.io.FileUtils.listFiles(
                        packageFolder,
                        new String[]{"js"},
                        true);
            }
            String file = p.getBase().substring(0, p.getBase().length() - 1) +
                    ".js";
            File js = new File(file);
            if (js.exists()) {
                files.add(js);
            }
            for (File f : files) {
                success = processSingle(f.getPath()) && success;
            }
        }
        // get code list
        ArrayList<String> codes = formCodeList();

        if (codes.size() > 0) {
            /*
                dependency 's output file encoding.
            */
            String outputEncoding = "utf-8";
            FileUtils.outputContent((this.compact ? COMPACT_CODE_PREFIX : CODE_PREFIX) +
                    ArrayUtils.join(codes.toArray(new String[codes.size()]), ",\n")
                    + CODE_SUFFIX
                    , output, outputEncoding);
            System.out.println("dependency file saved to: " + output);
        }

        System.out.println("duration: " + (System.currentTimeMillis() - start));

        if (!success) {
            System.exit(1);
        }
    }

    public static void main(String[] args) throws Exception {
        System.out.println("current path: " + new File(".").getAbsolutePath());
        System.out.println("current args: " + Arrays.toString(args));
        commandRunnerCLI(args);
    }
}
