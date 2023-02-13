package com.example.MyProject.controller;

import com.example.MyProject.model.AssetModel;
import com.example.MyProject.model.DeviceModel;
import com.example.MyProject.repo.AssetRepo;
import com.example.MyProject.repo.DeviceRepo;
import com.example.MyProject.service.AssetService;
import com.example.MyProject.service.DeviceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*", maxAge = 3600)
@RestController
@RequestMapping("/asset")
public class AssetController {
    public final AssetService assetService;
    public final AssetRepo assetRepo;


    @GetMapping
    public ResponseEntity<List<AssetModel>> getAllAssets() {
        return ResponseEntity.ok(assetService.getAllAssets());
    }

    @GetMapping("/active")
    public ResponseEntity<List<AssetModel>> getAllActiveAssets() {
        return ResponseEntity.ok(assetService.getAllActiveAssets());
    }

    @PostMapping
    public ResponseEntity<Void> createAsset() {
        assetRepo.save(new AssetModel("aboba", true));
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{name}")
    public ResponseEntity<Void> deleteAsset(@PathVariable String name){
        assetService.deleteAssetByName(name);
        return ResponseEntity.ok().build();
    }
    /*@GetMapping
    public ResponseEntity<List<Device>> getAllArticles() {
        return ResponseEntity.ok();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ArticleModel> getArticleByID(@PathVariable int id) {
        try {
            ArticleModel article = articleService.getArticleByID(id);
            return ResponseEntity.ok(article);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<Void> createArticle(@RequestBody ArticleDTO articleDTO) {
        final String title = articleDTO.getTitle();
        final String content = articleDTO.getContent();
        final Long categoryID = articleDTO.getCategoryID();
        final Long authorID = articleDTO.getAuthorID();
        try {
            final long id = articleService.createArticle(title, content, categoryID, authorID);
            final String location = String.format("/articles/%d", id);
            return ResponseEntity.created(URI.create(location)).build();
        } catch (Exception e){
            return ResponseEntity.badRequest().build();
        }
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Void> updateArticle (@PathVariable long id, @RequestBody ArticleDTO articleDTO){
        final String title = articleDTO.getTitle();
        final String content = articleDTO.getContent();
        final Long categoryID = articleDTO.getCategoryID();
        final Long authorID = articleDTO.getAuthorID();
        try {
            articleService.updateArticle(id, title, content, categoryID, authorID);
            return ResponseEntity.ok().build();
        } catch (Exception e){
            return ResponseEntity.badRequest().build();
        }
    }*/
}
