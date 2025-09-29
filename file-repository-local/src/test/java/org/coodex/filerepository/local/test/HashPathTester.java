package org.coodex.filerepository.local.test;

import org.coodex.filerepository.local.HashPathGenerator;

public class HashPathTester {
    public static void main(String[] args) {
        String fileName = "bcgl$16936cdc7f3511f09a937b0850071f1d";
        HashPathGenerator hashPathGenerator = new HashPathGenerator();
        System.out.printf(hashPathGenerator.getPath(fileName));
    }
}
