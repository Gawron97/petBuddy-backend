package com.example.petbuddybackend.repository.block;

import com.example.petbuddybackend.entity.block.Block;
import com.example.petbuddybackend.entity.block.BlockId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlockRepository extends JpaRepository<Block, BlockId> {
}
