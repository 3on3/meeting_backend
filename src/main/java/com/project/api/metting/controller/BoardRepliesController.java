package com.project.api.metting.controller;


import com.project.api.auth.TokenProvider.TokenUserInfo;
import com.project.api.metting.dto.request.BoardRepliesRequestDto;
import com.project.api.metting.dto.request.ReplyDeletRequestDto;
import com.project.api.metting.dto.response.BoardRepliesResponseDto;
import com.project.api.metting.service.BoardRepliesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Slf4j
@CrossOrigin
public class BoardRepliesController {

    private final BoardRepliesService boardRepliesService;


    //    댓글 GET
    @GetMapping("/board/replies")
    public ResponseEntity<?> getAllReplies(@RequestParam int pageNo, @RequestParam String boardId,@AuthenticationPrincipal TokenUserInfo tokenUserInfo){

        log.info("getReplies");

        try {
            Page<BoardRepliesResponseDto> boardReplies = boardRepliesService.getBoardReplies(pageNo, boardId,tokenUserInfo);
            log.info("Fetched board replies: {}",boardReplies);
            return ResponseEntity.ok().body(boardReplies);
        } catch (Exception e) {
            log.error("Error: {}",e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }

    }


    @PostMapping("/board/detail")
    public ResponseEntity<?> postAllReplies(@RequestBody BoardRepliesRequestDto dto, @AuthenticationPrincipal TokenUserInfo tokenUserInfo ){
        log.info("postAllReplies");


        BoardRepliesResponseDto boardRepliesResponseDto = boardRepliesService.postBoardReplies(dto, tokenUserInfo);
        return ResponseEntity.ok().body(boardRepliesResponseDto);
    }

    @PostMapping("/board/replyDelet")
    public ResponseEntity<?> deleteReplies(@RequestBody ReplyDeletRequestDto dto){

        log.info("deleteReplies");
        log.info("replyId :{}",dto);




        boardRepliesService.deleteBoardReply(dto);
        return ResponseEntity.ok().body("Delete success ");
    }



}
