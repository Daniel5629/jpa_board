package com.jpaboard.controller;

import com.jpaboard.model.Board;
import com.jpaboard.service.BoardService;
import com.jpaboard.validator.BoardValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.thymeleaf.util.StringUtils;

import javax.validation.Valid;

@Controller
@RequestMapping("/board")
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;
    private final BoardValidator boardValidator;


    @GetMapping("/list")
    public String list(Model model, @PageableDefault(size = 10) Pageable pageable,
                       @RequestParam(required = false, defaultValue = "") String searchText) {

        Page<Board> boards = boardService.getBoardsWithSearchText(searchText, searchText, pageable);

        int startPage = Math.max(1, boards.getPageable().getPageNumber() - 4);
        int endPage = Math.min(boards.getTotalPages(), boards.getPageable().getPageNumber() + 4);

        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);
        model.addAttribute("boards", boards);
        model.addAttribute("total", boards.getTotalElements());

        return "board/list";
    }

    @GetMapping("/form")
    public String form(Model model, @RequestParam(required = false) String mode, @RequestParam(required = false) Long id, Authentication authentication) {

        if (id == null) {
            model.addAttribute("board", new Board());
            return "board/formEdit";
        }

        Board board = boardService.getBoard(id);
        model.addAttribute("board", board);

        if (StringUtils.equals("U", mode)) {
            return "board/formEdit";
        }

        return "board/formRead";
    }

    @PostMapping("/form")
    public String post(@Valid Board board, @RequestParam(required = false) String mode, BindingResult bindingResult, Authentication authentication) {
        boardValidator.validate(board, bindingResult);

        if (bindingResult.hasErrors()) {
            return "board/formEdit";
        }

//        controller 외에서는 이런 방식으로 가져올 수 있다.
//        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        String username = authentication.getName();

        if (StringUtils.equals(mode, "U")) {
            boardService.updateBoard(username, board);
        } else {
            boardService.postBoard(username, board);
        }

        return "redirect:/board/list";
    }

    @PostMapping("/delete")
    public String delete(@RequestParam Long id, Authentication authentication) {

        String username = authentication.getName();

        boardService.deleteBoard(username, id);
        return "redirect:/board/list";
    }

    @GetMapping("/my-page")
    public String myPage() {




        return "board/myPage";
    }
}
