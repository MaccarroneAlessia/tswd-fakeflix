package edu.unict.tswd.fakeflix;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

@Entity
public class wlist {
    @Id
    @GeneratedValue
    private Long id;
    private String titolo;
    private String regista;

    public wlist() {}

    public wlist(String titolo, String regista) {
        this.titolo = titolo;
        this.regista = regista;
    }

    public String getTitolo() {
        return titolo;
    }

    public void setTitolo(String titolo) {
        this.titolo = titolo;
    }

    public String getRegista() {
        return regista;
    }

    public void setRegista(String regista) {
        this.regista = regista;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}

