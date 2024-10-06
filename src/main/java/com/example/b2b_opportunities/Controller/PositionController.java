package com.example.b2b_opportunities.Controller;

import com.example.b2b_opportunities.Dto.Request.PositionRequestDto;
import com.example.b2b_opportunities.Dto.Response.PositionResponseDto;
import com.example.b2b_opportunities.Entity.Position;
import com.example.b2b_opportunities.Mapper.PositionMapper;
import com.example.b2b_opportunities.Repository.PositionRepository;
import com.example.b2b_opportunities.Service.PositionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/positions")
@RequiredArgsConstructor
public class PositionController {
    private final PositionService positionService;
    private final PositionRepository positionRepository;

    @PostMapping()
    public PositionResponseDto createPosition(@RequestBody @Valid PositionRequestDto dto, Authentication authentication){
        return positionService.createPosition(dto, authentication);
    }
    @GetMapping
    public List<PositionResponseDto> getPositions(){
        List<Position> positions = positionRepository.findAll();
        List<PositionResponseDto> positionResponseDtoList = new ArrayList<>();
        for(Position position: positions){
            positionResponseDtoList.add(PositionMapper.toResponseDto(position));
        }
        return positionResponseDtoList;
    }
}