package com.example.melotalk.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.melotalk.entity.Artist;

@Repository
public interface ArtistRepository extends JpaRepository<Artist, String> {
    // 追加で外部ID検索や名前検索をしたい場合はここにメソッドを定義できます
    // 例: Optional<Artist> findByName(String name);
}
