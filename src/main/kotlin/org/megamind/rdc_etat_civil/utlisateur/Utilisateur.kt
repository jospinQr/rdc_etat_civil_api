package org.megamind.rdc_etat_civil.utlisat

import jakarta.persistence.*
import org.megamind.rdc_etat_civil.territoire.commune.Commune
import org.megamind.rdc_etat_civil.territoire.entite.Entite
import org.megamind.rdc_etat_civil.territoire.province.Province


@Entity
@Table(name = "users")
data class Utilisateur(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(unique = true, nullable = false)
    val username: String,

    @Column(nullable = false)
    val password: String,

    @Enumerated(EnumType.STRING)
    val role: Role = Role.ADMIN,


    @ManyToOne
    @JoinColumn(name = "ProvinceId")
    val province: Province? = null,


    @ManyToOne
    @JoinColumn(name = "EntiteId")
    val entite: Entite? = null,
   @ManyToOne
    @JoinColumn(name = "CommunId")
    val commune: Commune? = null


)

enum class Role {
    ADMIN,
    OEC,
    CB,
    CD,
}