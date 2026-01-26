package org.coodex.filerepository.local.test;

import org.coodex.filerepository.local.HashPathGenerator;

public class HashPathTester {
    public static void main(String[] args) {
        if (args.length > 0) {
            HashPathGenerator hashPathGenerator = new HashPathGenerator();
            for (String fileName : args) {
                System.out.println(hashPathGenerator.getPath(fileName));
            }
        }
    }
}
