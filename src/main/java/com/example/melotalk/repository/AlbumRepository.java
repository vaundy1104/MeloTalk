package com.example.melotalk.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.melotalk.entity.Album;

@Repository
public interface AlbumRepository extends JpaRepository<Album, String> {
    // 必要に応じて名前やアーティストで検索するメソッドも追加可能
    // 例: Optional<Album> findByTitle(String title);
}
