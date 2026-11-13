package com.example.evaluacion3controlequipos.DAO;

import com.example.evaluacion3controlequipos.database.JPAUtil;
import com.example.evaluacion3controlequipos.model.Equipo;
import com.example.evaluacion3controlequipos.model.TipoEquipo;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;
import java.util.List;

public class EquipoDAO {
    private TipoEquipo obtenerTipoEquipo(EntityManager em, Equipo equipo) {
        if (equipo.getTipoEquipo() == null || equipo.getTipoEquipo().getId() == null) {
            throw new IllegalArgumentException("Debe seleccionar un tipo de equipo.");
        }
        TipoEquipo tipo = em.find(TipoEquipo.class, equipo.getTipoEquipo().getId());
        if (tipo == null) throw new IllegalArgumentException("El tipo de equipo seleccionado no existe.");
        return tipo;
    }

    public void guardar(Equipo equipo) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            equipo.setTipoEquipo(obtenerTipoEquipo(em, equipo));
            em.persist(equipo);
            tx.commit();
        } catch (RuntimeException e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally { em.close(); }
    }

    public List<Equipo> listar() { return buscar(null, null); }

    public List<Equipo> buscar(String nombre, Long idTipo) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            String nombreFiltro = nombre == null ? "" : nombre.trim();
            StringBuilder jpql = new StringBuilder(
                    "SELECT e FROM Equipo e JOIN FETCH e.tipoEquipo t WHERE 1 = 1"
            );
            if (!nombreFiltro.isEmpty()) {
                jpql.append(" AND LOWER(e.nombre) LIKE LOWER(:nombre)");
            }
            if (idTipo != null) {
                jpql.append(" AND t.id = :idTipo");
            }
            jpql.append(" ORDER BY e.id");

            TypedQuery<Equipo> query = em.createQuery(jpql.toString(), Equipo.class);
            if (!nombreFiltro.isEmpty()) {
                query.setParameter("nombre", "%" + nombreFiltro + "%");
            }
            if (idTipo != null) {
                query.setParameter("idTipo", idTipo);
            }
            return query.getResultList();
        } finally {
            em.close();
        }
    }
    public boolean modificar(Equipo equipo) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Equipo actual = em.find(Equipo.class, equipo.getId());
            if (actual == null) { tx.rollback(); return false; }
            actual.setCodigo(equipo.getCodigo());
            actual.setNombre(equipo.getNombre());
            actual.setMarca(equipo.getMarca());
            actual.setEstado(equipo.getEstado());
            actual.setCantidad(equipo.getCantidad());
            actual.setTipoEquipo(obtenerTipoEquipo(em, equipo));
            tx.commit();
            return true;
        } catch (RuntimeException e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally { em.close(); }
    }

    public boolean eliminar(Long id) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Equipo actual = em.find(Equipo.class, id);
            if (actual == null) { tx.rollback(); return false; }
            em.remove(actual);
            tx.commit();
            return true;
        } catch (RuntimeException e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally { em.close(); }
    }
}

