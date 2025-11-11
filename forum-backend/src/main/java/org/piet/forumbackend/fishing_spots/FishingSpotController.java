package org.piet.forumbackend.fishing_spots;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.NoArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/spots")
@Tag(name = "Fishing Spot Controller", description = "Endpoints for fishing spots management")
@NoArgsConstructor
public class FishingSpotController {

}
