import java.util.LinkedList;
import java.util.Queue;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;

import java.awt.*;

public class PageReplacementSimulator extends JFrame {

    public PageReplacementSimulator(int fifoFaults, int lruFaults, int nfuFaults, int agingFaults) {
        initUI(fifoFaults, lruFaults, nfuFaults, agingFaults);
    }

    private void initUI(int fifoFaults, int lruFaults, int nfuFaults, int agingFaults) {
        // Cria o dataset para o gráfico de barras
        CategoryDataset dataset = createDataset(fifoFaults, lruFaults, nfuFaults, agingFaults);

        // Cria o gráfico de barras
        JFreeChart chart = ChartFactory.createBarChart(
                "Comparação de Algoritmos de Substituição de Página",
                "Algoritmo",
                "Faltas de Página",
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false);

        // Configura o painel de gráfico
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(800, 600));
        setContentPane(chartPanel);

        setTitle("Simulador de Substituição de Página");
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    // Cria o dataset com os valores das faltas de página
    private CategoryDataset createDataset(int fifoFaults, int lruFaults, int nfuFaults, int agingFaults) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(fifoFaults, "Faltas de Página", "FIFO");
        dataset.addValue(lruFaults, "Faltas de Página", "LRU");
        dataset.addValue(nfuFaults, "Faltas de Página", "NFU");
        dataset.addValue(agingFaults, "Faltas de Página", "Aging");
        return dataset;
    }

    // Métodos dos algoritmos
    private static int metodoFIFO(int[] sequenciaPaginas) {
        Queue<Integer> memoria = new LinkedList<>();
        int faltasPagina = 0;
        for (int pagina : sequenciaPaginas) {
            if (!memoria.contains(pagina)) {
                if (memoria.size() == 4) {
                    memoria.poll();
                }
                memoria.offer(pagina);
                faltasPagina++;
            }
        }
        return faltasPagina;
    }

    private static int metodoLRU(int[] sequenciaPaginas) {
        LinkedList<Integer> memoria = new LinkedList<>();
        int faltasPagina = 0;

        for (int pagina : sequenciaPaginas) {
            if (memoria.contains(pagina)) {
                memoria.remove((Integer) pagina);
            } else {
                if (memoria.size() == 4) {
                    memoria.poll();
                }
                faltasPagina++;
            }
            memoria.offer(pagina);
        }

        return faltasPagina;
    }

    private static int metodoNFU(int[] sequenciaPaginas) {
        Map<Integer, Integer> memoria = new HashMap<>();
        int faltasPagina = 0;

        for (int pagina : sequenciaPaginas) {
            if (memoria.containsKey(pagina)) {
                memoria.put(pagina, memoria.get(pagina) + 1);
            } else {
                if (memoria.size() == 4) {
                    int paginaMenosUsada = encontrarPaginaMenosUsada(memoria);
                    memoria.remove(paginaMenosUsada);
                }
                memoria.put(pagina, 1);
                faltasPagina++;
            }
        }

        return faltasPagina;
    }

    private static int encontrarPaginaMenosUsada(Map<Integer, Integer> memoria) {
        int menorContador = Integer.MAX_VALUE;
        int paginaMenosUsada = -1;

        for (Map.Entry<Integer, Integer> entry : memoria.entrySet()) {
            if (entry.getValue() < menorContador) {
                menorContador = entry.getValue();
                paginaMenosUsada = entry.getKey();
            }
        }

        return paginaMenosUsada;
    }

    private static int metodoAging(int[] sequenciaPaginas) {
        LinkedList<Integer> memoria = new LinkedList<>();
        LinkedList<Integer> bitsEnvelhecimento = new LinkedList<>();
        int faltasPagina = 0;

        for (int pagina : sequenciaPaginas) {
            if (memoria.contains(pagina)) {
                int indice = memoria.indexOf(pagina);
                bitsEnvelhecimento.set(indice, bitsEnvelhecimento.get(indice) | 0b10000000);
            } else {
                if (memoria.size() == 4) {
                    int paginaSubstituida = substituirPagina(memoria, bitsEnvelhecimento);
                    int indice = memoria.indexOf(paginaSubstituida);
                    memoria.set(indice, pagina);
                    bitsEnvelhecimento.set(indice, 0);
                } else {
                    memoria.add(pagina);
                    bitsEnvelhecimento.add(0);
                }
                faltasPagina++;
            }
            atualizarBitsEnvelhecimento(bitsEnvelhecimento);
        }

        return faltasPagina;
    }

    private static int substituirPagina(LinkedList<Integer> memoria, LinkedList<Integer> bitsEnvelhecimento) {
        int indiceSubstituir = 0;
        int menorBit = 0b10000000;

        for (int i = 0; i < memoria.size(); i++) {
            if (bitsEnvelhecimento.get(i) < menorBit) {
                menorBit = bitsEnvelhecimento.get(i);
                indiceSubstituir = i;
            }
        }

        return memoria.get(indiceSubstituir);
    }

    private static void atualizarBitsEnvelhecimento(LinkedList<Integer> bitsEnvelhecimento) {
        for (int i = 0; i < bitsEnvelhecimento.size(); i++) {
            bitsEnvelhecimento.set(i, bitsEnvelhecimento.get(i) >>> 1);
        }
    }

    public static void main(String[] args) {
        // Executa os algoritmos e obtém o número de faltas de página
        int[] sequenciaPaginas = {1, 2, 3, 4, 1, 2, 5, 1, 2, 3, 4, 5};

        int fifoFaults = metodoFIFO(sequenciaPaginas);
        int lruFaults = metodoLRU(sequenciaPaginas);
        int nfuFaults = metodoNFU(sequenciaPaginas);
        int agingFaults = metodoAging(sequenciaPaginas);

        // Cria a interface gráfica com os resultados
        SwingUtilities.invokeLater(() -> {
            PageReplacementSimulator ex = new PageReplacementSimulator(fifoFaults, lruFaults, nfuFaults, agingFaults);
            ex.setVisible(true);
        });
    }
}
