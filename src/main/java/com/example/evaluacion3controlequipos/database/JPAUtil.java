package com.example.evaluacion3controlequipos.database;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.util.HashMap;
import java.util.Map;

public class JPAUtil {
    private static EntityManagerFactory entityManagerFactory;

    public static synchronized void iniciar() {
        if (entityManagerFactory == null) {
            String url = variableRequerida("DB_URL");
            String usuario = variableRequerida("DB_USER");
            String password = variableRequerida("DB_PASSWORD");

            Map<String, String> properties = new HashMap<>();
            properties.put("jakarta.persistence.jdbc.url", url);
            properties.put("jakarta.persistence.jdbc.user", usuario);
            properties.put("jakarta.persistence.jdbc.password", password);

            entityManagerFactory =
                    Persistence.createEntityManagerFactory("laboratorioPU", properties);
        }
    }

    private static String variableRequerida(String nombre) {
        String valor = System.getenv(nombre);
        if (valor == null || valor.trim().isEmpty()) {
            throw new IllegalStateException(
                    "La variable de entorno " + nombre + " no está configurada en IntelliJ."
            );
        }
        return valor;
    }

    public static EntityManager getEntityManager() {
        iniciar();
        return entityManagerFactory.createEntityManager();
    }

    public static synchronized void cerrar() {
        if (entityManagerFactory != null && entityManagerFactory.isOpen()) {
            entityManagerFactory.close();
            entityManagerFactory = null;
        }
    }
}
