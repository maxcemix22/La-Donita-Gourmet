/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Modelo;

/**
 *
 * @author Administrator
 */
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.List;

public class Pedido {
    private String idPedido;
    private LocalDateTime fecha;
    private LocalDate fechaEntreega; 
    private String estado;    
    private String tipo;     
    private BigDecimal total; 

private Cliente cliente;
public Pedido(String idPedido, LocalDateTime fecha, LocalDate fechaEntreega, 
                  String estado, String tipo, BigDecimal total, Cliente cliente) {
        this.idPedido = idPedido;
        this.fecha = fecha;
        this.fechaEntreega = fechaEntreega;
        this.estado = estado;
        this.tipo = tipo;
        this.total = total;
        this.cliente = cliente;


}        
}