package ru.javaops.masterjava.matrix;

import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

/**
 * gkislin
 * 03.07.2016
 */
public class MatrixUtil {

    public static int[][] concurrentMultiply(int[][] matrixA, int[][] matrixB, ExecutorService executor) throws InterruptedException, ExecutionException {
        final int matrixSize = matrixA.length;
        final int[][] matrixC = new int[matrixSize][matrixSize];
        //final Stack<Integer> rowNumber = new Stack<>();
        final CountDownLatch latch = new CountDownLatch(matrixSize);

        // what to do with results if use executor.invokeAll?
/*        final List<Callable<Integer>> tasks = new ArrayList<>();

        for (int i = 0; i < matrixSize; i++) {
            rowNumber.push(i);
            tasks.add(() -> {
                int[] columnB = new int[matrixSize];
                int row = rowNumber.pop();

                for (int j = 0; j < matrixSize; j++) {
                    columnB[j] = matrixB[j][row];
                }

                for (int j = 0; j < matrixSize; j++) {
                    int sum = 0;
                    int[] rowA = matrixA[j];

                    for (int k = 0; k < matrixSize; k++) {
                        sum += rowA[k] * columnB[k];
                    }
                    matrixC[j][row] = sum;
                }
                return 0;
            });
        }
        executor.invokeAll(tasks);*/


        for (int i = 0; i < matrixSize; i++) {
            final int row = i;
            executor.submit(() -> {
                int[] columnB = new int[matrixSize];

                for (int j = 0; j < matrixSize; j++) {
                    columnB[j] = matrixB[j][row];
                }

                for (int j = 0; j < matrixSize; j++) {
                    int sum = 0;
                    int[] rowA = matrixA[j];

                    for (int k = 0; k < matrixSize; k++) {
                        sum += rowA[k] * columnB[k];
                    }
                    matrixC[j][row] = sum;
                }
                latch.countDown();
            });
        }

        latch.await();
        return matrixC;
    }

    public static int[][] singleThreadMultiply(final int[][] matrixA, final int[][] matrixB) {
        final int matrixSize = matrixA.length;
        final int[][] matrixC = new int[matrixSize][matrixSize];
        final int[] columnB = new int[matrixSize];

        try {
            for (int i = 0; ; i++) {

                for (int j = 0; j < matrixSize; j++) {
                    columnB[j] = matrixB[j][i];
                }

                for (int j = 0; j < matrixSize; j++) {
                    int sum = 0;
                    int[] rowA = matrixA[j];

                    for (int k = 0; k < matrixSize; k++) {
                        sum += rowA[k]*columnB[k];
                    }
                    matrixC[j][i] = sum;
                }
            }
        } catch (IndexOutOfBoundsException e) {}

        return matrixC;
    }

    public static int[][] create(int size) {
        int[][] matrix = new int[size][size];
        Random rn = new Random();

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                matrix[i][j] = rn.nextInt(10);
            }
        }
        return matrix;
    }

    public static boolean compare(int[][] matrixA, int[][] matrixB) {
        final int matrixSize = matrixA.length;
        for (int i = 0; i < matrixSize; i++) {
            for (int j = 0; j < matrixSize; j++) {
                if (matrixA[i][j] != matrixB[i][j]) {
                    return false;
                }
            }
        }
        return true;
    }
}
