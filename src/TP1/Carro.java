package TP1;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Esta classe é Serializable para poder ser
 * escrita no ficheiro de texto "carros.txt"
 */
public class Carro implements Serializable {

    private int id;
    private double preco_aluguer;
    private String estado;
    private String nome;
    private String descricao;
    private String nome_dono_atual;
    private ArrayList<Integer> reservas = new ArrayList();

    public Carro (String nome, String descricao, double preco) {
        this.nome = nome;
        this.descricao = descricao;
        this.preco_aluguer = preco;
    }

    /**
     * O dono atual é aquele que atualmente possui o recurso.
     * @param nome_dono_atual nome do dono atual
     */
    public void setDono_atual(String nome_dono_atual) {
        this.nome_dono_atual = nome_dono_atual;
    }

    /**
     *
     * @return nome do carro
     */
    public String getNome() {
        return nome;
    }

    /**
     *
     * @return identificador do carro
     */
    public int getId() {
        return id;
    }

    /**
     *
     * @param id a ser atribuído ao carro
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     *
     * @return estado atual do carro: disponível, requisitado ou reservado
     */
    public String getEstado() {
        return estado;
    }

    /**
     *
     * @param estado novo estado para o carro
     */
    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getDescricao() {
        return descricao;
    }

    /**
     *
     * @return devolve a lista das reservas
     */
    public ArrayList getReservas() {
        return reservas;
    }

    /**
     *
     * @param id_cliente número do cliente que quer reservar este carro
     */
    public void addReserva (int id_cliente) {
        this.reservas.add(id_cliente);
    }

    @Override
    public String toString() {

        // Se está disponível
        if (estado.equals("disponível"))
            return "\n\tNome: " + nome
                    + "\n\tDescrição: " + descricao
                    + "\n\tPreço: " + preco_aluguer
                    + "\n\tEstado: " + estado
                    + "\n\tId: " + id
                    + "\n";

        // Se não está disponível
        else {
            // Se tem reservas
            if (reservas.size() > 0) {
                return "\n\tNome: " + nome
                        + "\n\tDescrição: " + descricao
                        + "\n\tPreço: " + preco_aluguer
                        + "\n\tEstado: requisitado por " + nome_dono_atual
                        + "\n\t\t\treservado por " + reservas.size() + " pessoas"
                        + "\n\tId: " + id
                        + "\n";
            }

            // Se não tem reservas
            else {
                return "\n\tNome: " + nome
                        + "\n\tDescrição: " + descricao
                        + "\n\tPreço: " + preco_aluguer
                        + "\n\tEstado: requisitado por " + nome_dono_atual
                        + "\n\tId: " + id
                        + "\n";
            }
        }
    }
}
