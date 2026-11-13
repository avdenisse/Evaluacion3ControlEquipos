package com.example.evaluacion3controlequipos.controller;

import com.example.evaluacion3controlequipos.DAO.EquipoDAO;
import com.example.evaluacion3controlequipos.DAO.TipoEquipoDAO;
import com.example.evaluacion3controlequipos.model.Equipo;
import com.example.evaluacion3controlequipos.model.TipoEquipo;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;
import java.util.List;
import java.util.Optional;

public class EquipoController {
    @FXML private TextField txtCodigo, txtNombre, txtMarca, txtCantidad, txtBuscarNombre;
    @FXML private ComboBox<TipoEquipo> cmbTipoEquipo, cmbFiltroTipo;
    @FXML private ComboBox<String> cmbEstado;
    @FXML private Button btnGuardar, btnActualizar, btnEliminar;
    @FXML private TableView<Equipo> tblEquipos;
    @FXML private TableColumn<Equipo, String> colCodigo, colNombre, colMarca, colEstado;
    @FXML private TableColumn<Equipo, Integer> colCantidad;
    @FXML private TableColumn<Equipo, TipoEquipo> colTipoEquipo;

    private final EquipoDAO equipoDAO = new EquipoDAO();
    private final TipoEquipoDAO tipoEquipoDAO = new TipoEquipoDAO();
    private Long idSeleccionado = null;

    @FXML public void initialize() {
        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colMarca.setCellValueFactory(new PropertyValueFactory<>("marca"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
        colCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colTipoEquipo.setCellValueFactory(new PropertyValueFactory<>("tipoEquipo"));
        StringConverter<TipoEquipo> converter = new StringConverter<TipoEquipo>() {
            @Override public String toString(TipoEquipo tipo) { return tipo == null ? "" : tipo.getNombre(); }
            @Override public TipoEquipo fromString(String texto) { return null; }
        };
        cmbTipoEquipo.setConverter(converter);
        cmbFiltroTipo.setConverter(converter);
        colTipoEquipo.setCellFactory(column -> new TableCell<Equipo, TipoEquipo>() {
            @Override protected void updateItem(TipoEquipo tipo, boolean empty) {
                super.updateItem(tipo, empty);
                setText(empty || tipo == null ? null : tipo.getNombre());
            }
        });
        cmbEstado.setItems(FXCollections.observableArrayList("Disponible", "En mantenimiento", "Fuera de servicio"));
        tblEquipos.getSelectionModel().selectedItemProperty().addListener((obs, anterior, equipo) -> {
            if (equipo != null) seleccionarEquipo(equipo);
            else { idSeleccionado = null; actualizarBotones(); }
        });
        cargarTipos();
        actualizarBotones();
        cargarEquipos();
    }

    private void cargarTipos() {
        try {
            List<TipoEquipo> tipos = tipoEquipoDAO.listar();
            cmbTipoEquipo.setItems(FXCollections.observableArrayList(tipos));
            cmbFiltroTipo.setItems(FXCollections.observableArrayList(tipos));
        } catch (Exception e) { mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudieron cargar los tipos de equipo.", e.getMessage()); }
    }

    private void seleccionarEquipo(Equipo equipo) {
        idSeleccionado = equipo.getId();
        txtCodigo.setText(equipo.getCodigo());
        txtNombre.setText(equipo.getNombre());
        txtMarca.setText(equipo.getMarca());
        txtCantidad.setText(String.valueOf(equipo.getCantidad()));
        cmbEstado.setValue(equipo.getEstado());
        cmbTipoEquipo.setValue(equipo.getTipoEquipo());
        actualizarBotones();
    }

    private void actualizarBotones() {
        boolean sinSeleccion = idSeleccionado == null;
        btnGuardar.setDisable(!sinSeleccion);
        btnActualizar.setDisable(sinSeleccion);
        btnEliminar.setDisable(sinSeleccion);
    }

    private Equipo obtenerEquipoFormulario() {
        String codigo = txtCodigo.getText() == null ? "" : txtCodigo.getText().trim();
        String nombre = txtNombre.getText() == null ? "" : txtNombre.getText().trim();
        String marca = txtMarca.getText() == null ? "" : txtMarca.getText().trim();
        String cantidadTexto = txtCantidad.getText() == null ? "" : txtCantidad.getText().trim();
        if (codigo.isEmpty() || nombre.isEmpty() || marca.isEmpty() || cantidadTexto.isEmpty())
            throw new IllegalArgumentException("Complete código, nombre, marca y cantidad.");
        int cantidad;
        try { cantidad = Integer.parseInt(cantidadTexto); }
        catch (NumberFormatException e) { throw new IllegalArgumentException("La cantidad debe ser un número entero."); }
        if (cantidad < 0) throw new IllegalArgumentException("La cantidad no puede ser negativa.");
        if (cmbEstado.getValue() == null) throw new IllegalArgumentException("Seleccione un estado.");
        if (cmbTipoEquipo.getValue() == null) throw new IllegalArgumentException("Seleccione un tipo de equipo.");
        return new Equipo(codigo, nombre, marca, cmbEstado.getValue(), cantidad, cmbTipoEquipo.getValue());
    }

    @FXML private void guardarEquipo() {
        try { equipoDAO.guardar(obtenerEquipoFormulario()); limpiarFormulario(); cargarEquipos(); mostrarAlerta(Alert.AlertType.INFORMATION, "Guardado", "El equipo se guardó correctamente.", null); }
        catch (Exception e) { mostrarAlerta(Alert.AlertType.ERROR, "No se pudo guardar", "Revise los datos del equipo.", e.getMessage()); }
    }

    @FXML private void actualizarEquipo() {
        if (idSeleccionado == null) return;
        try {
            Equipo equipo = obtenerEquipoFormulario(); equipo.setId(idSeleccionado);
            if (!equipoDAO.modificar(equipo)) { mostrarAlerta(Alert.AlertType.WARNING, "Sin cambios", "No se encontró el equipo seleccionado.", null); return; }
            limpiarFormulario(); cargarEquipos(); mostrarAlerta(Alert.AlertType.INFORMATION, "Actualizado", "El equipo se actualizó correctamente.", null);
        } catch (Exception e) { mostrarAlerta(Alert.AlertType.ERROR, "No se pudo actualizar", "Revise los datos del equipo.", e.getMessage()); }
    }

    @FXML private void eliminarEquipo() {
        if (idSeleccionado == null) return;
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION, "¿Desea eliminar el equipo seleccionado?", ButtonType.CANCEL, ButtonType.OK);
        confirmacion.setTitle("Confirmar eliminación"); confirmacion.setHeaderText("Eliminar equipo");
        Optional<ButtonType> respuesta = confirmacion.showAndWait();
        if (!respuesta.isPresent() || respuesta.get() != ButtonType.OK) return;
        try {
            if (!equipoDAO.eliminar(idSeleccionado)) { mostrarAlerta(Alert.AlertType.WARNING, "Sin cambios", "No se encontró el equipo seleccionado.", null); return; }
            limpiarFormulario(); cargarEquipos(); mostrarAlerta(Alert.AlertType.INFORMATION, "Eliminado", "El equipo se eliminó correctamente.", null);
        } catch (Exception e) { mostrarAlerta(Alert.AlertType.ERROR, "No se pudo eliminar", "Ocurrió un error al eliminar el equipo.", e.getMessage()); }
    }

    @FXML private void buscarEquipos() {
        try {
            TipoEquipo tipo = cmbFiltroTipo.getValue();
            List<Equipo> equipos = equipoDAO.buscar(txtBuscarNombre.getText(), tipo == null ? null : tipo.getId());
            tblEquipos.setItems(FXCollections.observableArrayList(equipos));
            if (equipos.isEmpty()) mostrarAlerta(Alert.AlertType.INFORMATION, "Búsqueda", "No se encontraron equipos con esos filtros.", null);
        } catch (Exception e) { mostrarAlerta(Alert.AlertType.ERROR, "Error de búsqueda", "No se pudieron consultar los equipos.", e.getMessage()); }
    }

    @FXML private void cargarEquipos() {
        try { tblEquipos.setItems(FXCollections.observableArrayList(equipoDAO.listar())); }
        catch (Exception e) { mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudieron cargar los equipos.", e.getMessage()); }
    }

    @FXML private void limpiarFormulario() {
        tblEquipos.getSelectionModel().clearSelection();
        idSeleccionado = null;
        txtCodigo.clear(); txtNombre.clear(); txtMarca.clear(); txtCantidad.clear();
        cmbTipoEquipo.getSelectionModel().clearSelection(); cmbEstado.getSelectionModel().clearSelection();
        actualizarBotones();
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String encabezado, String detalle) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo); alerta.setHeaderText(encabezado); alerta.setContentText(detalle == null ? "" : detalle); alerta.showAndWait();
    }
}
