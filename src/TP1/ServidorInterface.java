package TP1;

import java.util.ArrayList;

public interface ServidorInterface extends java.rmi.Remote {

    public void subscribe (ClienteInterface c) throws java.rmi.RemoteException;
    public String inserirRecurso (String nomeCarro, String descricaoCarro, double precoCarro) throws java.rmi.RemoteException;
    public boolean existemRecursos () throws java.rmi.RemoteException;
    public void consultarRecursos (ClienteInterface c, String s) throws java.rmi.RemoteException;
    public void requisitarRecurso (ClienteInterface c, int id) throws java.rmi.RemoteException;
    public void devolverRecurso (ClienteInterface c, int id) throws java.rmi.RemoteException;
    public String todosRecursos () throws java.rmi.RemoteException;
    public ArrayList<Integer> clienteCarrosRequisitados (ClienteInterface c) throws java.rmi.RemoteException;
}
