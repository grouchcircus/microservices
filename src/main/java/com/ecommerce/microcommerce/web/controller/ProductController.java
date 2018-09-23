package com.ecommerce.microcommerce.web.controller;

import com.ecommerce.microcommerce.dao.ProductDao;
import com.ecommerce.microcommerce.model.Product;
import com.ecommerce.microcommerce.web.exceptions.ProduitIntrouvableException;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import java.net.URI;
import java.util.List;

@Api( description="API pour les opérations sur produits")
@RestController
public class ProductController {

    @Autowired
    private ProductDao productDao;


    //Récupérer la liste des produits
    @ApiOperation(value = "Consulter la liste des produits")
    @RequestMapping(value = "/Produits", method = RequestMethod.GET)

    public MappingJacksonValue listeProduits() {

        List<Product> produits = productDao.findAll();

        SimpleBeanPropertyFilter monFiltre = SimpleBeanPropertyFilter.serializeAllExcept("prixAchat");
        FilterProvider listDeNosFiltres = new SimpleFilterProvider().addFilter("monFiltreDynamique", monFiltre);
        MappingJacksonValue produitsFiltres = new MappingJacksonValue(produits);
        produitsFiltres.setFilters(listDeNosFiltres);

        return produitsFiltres;
    }

    //Récupérer un produit par son Id
    @ApiOperation(value = "Sélectionner un produit par son identifiant")
    @GetMapping(value = "/Produits/{id}")

    public Product afficherUnProduit(@PathVariable int id) {

        Product produit = productDao.findById(id);

        if (produit==null) throw new ProduitIntrouvableException("Le produit avec l'id " + id + " est INTROUVABLE");

        return productDao.findById(id);
    }

    //Filtrer les produits dont le prix est supérieur à x
    @ApiOperation(value = "Filtrer les produits dont le prix est supérieur à une valeur limite")
    @GetMapping(value = "Produits/PrixLimit/{prixLimit}")
    public List<Product> filtrerParPrixLimit(@PathVariable int prixLimit) {
        return productDao.findByPrixGreaterThan(prixLimit);
    }

    //Filtrer les produits dont le nom est x
    @ApiOperation(value = "Chercher un produit par son libellé")
    @GetMapping(value = "Produits/SearchByName/{recherche}")
    public List<Product> findByNomLike(@PathVariable String recherche) {
        return productDao.findByNomLike("%"+recherche+"%");
    }

    //Ajouter un produit
    @ApiOperation(value = "Créer un produit avec ses caractéristiques")
    @PostMapping(value = "/Produits")
    public ResponseEntity<Void> ajouterProduit(@Valid @RequestBody Product product) {

        Product productAdded =  productDao.save(product);

        if (productAdded == null)
            return ResponseEntity.noContent().build();

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(productAdded.getId())
                .toUri();

        return ResponseEntity.created(location).build();
    }

    /*Supprimer un produit par son Id
    @DeleteMapping(value = "/Produits/{id}")

    public Product supprimerUnProduit(@PathVariable int id) {

        return productDao.delete(id);
    }*/

    //Updater un produit
    @ApiOperation(value = "Mettre à jour les informations d'un produit via son identifiant")
    @PutMapping (value = "/Produits")
    public void updateProduit(@RequestBody Product product) {

        productDao.save(product);
    }

}