package com.example.evaluacion3controlequipos.DAO;

import com.example.evaluacion3controlequipos.database.JPAUtil;
import com.example.evaluacion3controlequipos.model.TipoEquipo;
import jakarta.persistence.EntityManager;

import java.util.List;

public class TipoEquipoDAO {
    public List<TipoEquipo> listar(){
        EntityManager em = JPAUtil.getEntityManager();
        try{
            return em.createQuery("select t from TipoEquipo t order by t.nombre", TipoEquipo.class).getResultList();

        }finally{
            em.close();
        }
    }
}

