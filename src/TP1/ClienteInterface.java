package TP1;

import java.util.ArrayList;

public interface ClienteInterface extends java.rmi.Remote {

    public void printNoCliente (String s) throws java.rmi.RemoteException;
    public void setNumero(int numero) throws java.rmi.RemoteException;
    public int getNumero() throws java.rmi.RemoteException;
    public String getNome() throws java.rmi.RemoteException;
    public ArrayList getRequisitados() throws java.rmi.RemoteException;
    public void setRequisitados(ArrayList requisitados) throws java.rmi.RemoteException;
    public void addRequisicoes(int carro) throws java.rmi.RemoteException;
}
