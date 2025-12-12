package org.piet.forumbackend.fish.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.piet.forumbackend.fish.dtos.CreateFishDto;
import org.piet.forumbackend.fish.dtos.FishDto;
import org.piet.forumbackend.fish.dtos.FishDtoMapper;
import org.piet.forumbackend.fish.entities.Fish;
import org.piet.forumbackend.fish.services.FishService;
import org.piet.forumbackend.globals.exceptions.UnauthorizedAccessException;
import org.piet.forumbackend.users.core.entities.User;
import org.piet.forumbackend.users.core.exceptions.UserNotLoggedInException;
import org.piet.forumbackend.users.core.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("${forum.api.prefix}/admin/fish")
@RequiredArgsConstructor
@Tag(name = "Admin - fish", description = "Endpoints for admin to manage fish.")
public class FishAdminController {
    private final UserService userService;
    private final FishService fishService;


    @PostMapping("/fish/create")
    public ResponseEntity<FishDto> createFish(@ModelAttribute CreateFishDto fishDto, Authentication auth) throws UserNotLoggedInException, IOException {
        User user = userService.getUserFromAuth(auth);
        Fish fish = fishService.createFish(
                fishDto.getName(),
                fishDto.getDescription(),
                fishDto.getAvgLengthCm(),
                fishDto.getAvgMassKg(),
                fishDto.isPredatory(),
                fishDto.getPhoto(),
                fishDto.getMethods(),
                fishDto.getWaterType(), user);
        return ResponseEntity.ok(FishDtoMapper.toFishDto(fish));
    }

    @DeleteMapping("/fish/delete")
    public ResponseEntity<?> deleteFish(@RequestParam String name, Authentication auth) throws UserNotLoggedInException, UnauthorizedAccessException {
        User u = userService.getUserFromAuth(auth);
        fishService.deleteFishByName(name, u);
        return ResponseEntity.ok().build();
    }
}
