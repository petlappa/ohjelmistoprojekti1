package fi.haagahelia.ticketguru.domain;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;

@Entity
public class Kayttaja {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String kayttajanimi;

    @Column(nullable = false)
    private String salasana;

    @Column(nullable = false, length = 80)
    private String etunimi;

    @Column(nullable = false, length = 80)
    private String sukunimi;

    @ManyToOne(optional = false)
    @JoinColumn(name = "rooli_id", nullable = false)
    private Rooli rooli;

    @OneToMany(mappedBy = "myyja")
    private List<Myyntitapahtuma> myyntitapahtumat = new ArrayList<>();

    public Kayttaja() {
    }

    public Kayttaja(String kayttajanimi, String salasana, String etunimi, String sukunimi, Rooli rooli) {
        this.kayttajanimi = kayttajanimi;
        this.salasana = salasana;
        this.etunimi = etunimi;
        this.sukunimi = sukunimi;
        this.rooli = rooli;
    }

    public Long getId() {
        return id;
    }

    public String getKayttajanimi() {
        return kayttajanimi;
    }

    public void setKayttajanimi(String kayttajanimi) {
        this.kayttajanimi = kayttajanimi;
    }

    public String getSalasana() {
        return salasana;
    }

    public void setSalasana(String salasana) {
        this.salasana = salasana;
    }

    public String getEtunimi() {
        return etunimi;
    }

    public void setEtunimi(String etunimi) {
        this.etunimi = etunimi;
    }

    public String getSukunimi() {
        return sukunimi;
    }

    public void setSukunimi(String sukunimi) {
        this.sukunimi = sukunimi;
    }

    public Rooli getRooli() {
        return rooli;
    }

    public void setRooli(Rooli rooli) {
        this.rooli = rooli;
    }

    public List<Myyntitapahtuma> getMyyntitapahtumat() {
        return myyntitapahtumat;
    }

    public void setMyyntitapahtumat(List<Myyntitapahtuma> myyntitapahtumat) {
        this.myyntitapahtumat = myyntitapahtumat;
    }
}
