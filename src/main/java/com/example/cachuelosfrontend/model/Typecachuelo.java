package com.example.cachuelosfrontend.model;

// Generated 05-oct-2014 14:18:06 by Hibernate Tools 3.4.0.CR1

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;

import static javax.persistence.GenerationType.IDENTITY;

import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.codehaus.jackson.annotate.JsonIgnore;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 * Typecachuelo generated by hbm2java
 */
@Entity
@Table(name = "typecachuelo"
      , catalog = "cachuelos")
@XmlRootElement
public class Typecachuelo implements java.io.Serializable
{

   private Integer idTypeCachuelo;
   private String name;
   private String description;
   private Set<Cachuelo> cachuelos = new HashSet<Cachuelo>(0);
   private Set<Workerbytypecachuelo> workerbytypecachuelos = new HashSet<Workerbytypecachuelo>(0);
   private Set<Digitalizeddocument> digitalizeddocuments = new HashSet<Digitalizeddocument>(0);

   public Typecachuelo()
   {
   }

   public Typecachuelo(String name)
   {
      this.name = name;
   }

   public Typecachuelo(String name, String description, Set<Cachuelo> cachuelos, Set<Workerbytypecachuelo> workerbytypecachuelos, Set<Digitalizeddocument> digitalizeddocuments)
   {
      this.name = name;
      this.description = description;
      this.cachuelos = cachuelos;
      this.workerbytypecachuelos = workerbytypecachuelos;
      this.digitalizeddocuments = digitalizeddocuments;
   }

   @Id
   @GeneratedValue(strategy = IDENTITY)
   @Column(name = "idTypeCachuelo", unique = true, nullable = false)
   public Integer getIdTypeCachuelo()
   {
      return this.idTypeCachuelo;
   }

   public void setIdTypeCachuelo(Integer idTypeCachuelo)
   {
      this.idTypeCachuelo = idTypeCachuelo;
   }

   @Column(name = "name", nullable = false, length = 20)
   public String getName()
   {
      return this.name;
   }

   public void setName(String name)
   {
      this.name = name;
   }

   @Column(name = "description", length = 200)
   public String getDescription()
   {
      return this.description;
   }

   public void setDescription(String description)
   {
      this.description = description;
   }

   @XmlTransient
   @JsonIgnore
   @OneToMany(fetch = FetchType.LAZY, mappedBy = "typecachuelo")
   public Set<Cachuelo> getCachuelos()
   {
      return this.cachuelos;
   }

   public void setCachuelos(Set<Cachuelo> cachuelos)
   {
      this.cachuelos = cachuelos;
   }

   @XmlTransient
   @JsonIgnore
   @OneToMany(fetch = FetchType.LAZY, mappedBy = "typecachuelo")
   public Set<Workerbytypecachuelo> getWorkerbytypecachuelos()
   {
      return this.workerbytypecachuelos;
   }

   public void setWorkerbytypecachuelos(Set<Workerbytypecachuelo> workerbytypecachuelos)
   {
      this.workerbytypecachuelos = workerbytypecachuelos;
   }

   @XmlTransient
   @JsonIgnore
   @OneToMany(fetch = FetchType.LAZY, mappedBy = "typecachuelo")
   public Set<Digitalizeddocument> getDigitalizeddocuments()
   {
      return this.digitalizeddocuments;
   }

   public void setDigitalizeddocuments(Set<Digitalizeddocument> digitalizeddocuments)
   {
      this.digitalizeddocuments = digitalizeddocuments;
   }
   
   public String toString() {
		StringBuilder buffer = new StringBuilder();
		buffer.append(name);
		return buffer.toString();
	}

}
