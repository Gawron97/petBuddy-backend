package com.example.petbuddybackend.repository.block;

import com.example.petbuddybackend.entity.block.Block;
import com.example.petbuddybackend.entity.block.BlockId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BlockRepository extends JpaRepository<Block, BlockId> {

    List<Block> findByBlockerEmail(String username);
}
