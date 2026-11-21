package com.proyecto.ecommerce.model;

import jakarta.persistence.*;

@Entity
@Table(name = "productos")
public class Producto {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  private String nombre;

  @Column(length = 2000)
  private String descripcion;

  private String imagen;
  private double precio;
  private int cantidad;

  @ManyToOne
  private Usuario usuario;

  public Producto() {
    // Constructor vacío
  }

  public Producto(
    Integer id,
    String nombre,
    String descripcion,
    String imagen,
    double precio,
    int cantidad,
    Usuario usuario
  ) {
    super();
    this.id = id;
    this.nombre = nombre;
    this.descripcion = descripcion;
    this.imagen = imagen;
    this.precio = precio;
    this.cantidad = cantidad;
    this.usuario = usuario;
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getNombre() {
    return nombre;
  }

  public void setNombre(String nombre) {
    this.nombre = nombre;
  }

  public String getDescripcion() {
    return descripcion;
  }

  public void setDescripcion(String descripcion) {
    this.descripcion = descripcion;
  }

  public String getImagen() {
    return imagen;
  }

  @Transient
  public String getCategoria() {
    String n = nombre == null ? "" : nombre.toLowerCase(java.util.Locale.ROOT);
    if (
      n.contains("laptop") ||
      n.contains("asus") ||
      n.contains("tablet") ||
      n.contains("monitor")
    ) return "Computación";
    if (
      n.contains("sony") || n.contains("yeti") || n.contains("echo")
    ) return "Audio";
    if (
      n.contains("switch") ||
      n.contains("silla") ||
      n.contains("mouse") ||
      n.contains("teclado")
    ) return "Gaming";
    return "Hogar y oficina";
  }

  public void setImagen(String imagen) {
    this.imagen = imagen;
  }

  public double getPrecio() {
    return precio;
  }

  public void setPrecio(double precio) {
    this.precio = precio;
  }

  public int getCantidad() {
    return cantidad;
  }

  public void setCantidad(int cantidad) {
    this.cantidad = cantidad;
  }

  public Usuario getUsuario() {
    return usuario;
  }

  public void setUsuario(Usuario usuario) {
    this.usuario = usuario;
  }

  @Override
  public String toString() {
    return (
      "Producto{" +
      "id=" +
      id +
      ", nombre='" +
      nombre +
      '\'' +
      ", descripcion='" +
      descripcion +
      '\'' +
      ", imagen='" +
      imagen +
      '\'' +
      ", precio=" +
      precio +
      ", cantidad=" +
      cantidad +
      '}'
    );
  }
}
