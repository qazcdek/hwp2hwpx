package kr.dogfoot.hwp2hwpx.section.object;

import kr.dogfoot.hwp2hwpx.util.ValueConvertor;
import kr.dogfoot.hwplib.object.bodytext.control.ControlOverlappingLetter;
import kr.dogfoot.hwplib.object.etc.HWPString;
import kr.dogfoot.hwpxlib.object.content.section_xml.enumtype.ComposeCircleType;
import kr.dogfoot.hwpxlib.object.content.section_xml.enumtype.ComposeType;
import kr.dogfoot.hwpxlib.object.content.section_xml.paragraph.Compose;

import java.util.List; // [추가] List 임포트

public class ForCompose {
    public static void convert(Compose compose, ControlOverlappingLetter hwpOverlappingLetter) {
        // [수정] 겹쳐 쓸 글자 목록을 가져옵니다.
        List<HWPString> letterList = hwpOverlappingLetter.getHeader().getOverlappingLetterList();

        // [수정] 가장 중요한 방어 코드: 목록이 null이거나 비어있는지 확인합니다.
        if (letterList == null || letterList.isEmpty()) {
            System.err.println("경고: 글자 겹침(OverlappingLetter) 컨트롤에 처리할 문자가 없습니다. 공백 문자로 대체합니다.");
            // [수정] 종료하는 대신, 이 compose 객체를 공백 문자로 설정합니다.
            compose
                    .circleTypeAnd(ComposeCircleType.CHAR)
                    .charSzAnd((short) hwpOverlappingLetter.getHeader().getInternalFontSize())
                    .composeType(ComposeType.SPREAD);
            compose.composeText(" ");
            return; // 공백으로 대체 처리가 끝났으므로 메서드를 종료합니다.
        }

        // [수정] 이제 목록이 비어있지 않다는 것이 보장되었으므로, 안전하게 첫 번째 글자를 가져옵니다.
        String firstCharString = letterList.get(0).toUTF16LEString();

        compose
                .circleTypeAnd(composeCircleType(firstCharString)) // [수정] 안전하게 가져온 첫 글자 사용
                .charSzAnd((short) hwpOverlappingLetter.getHeader().getInternalFontSize())
                .composeType(composeType(hwpOverlappingLetter.getHeader().getExpendInsideLetter()));

        composeText(compose, hwpOverlappingLetter, firstCharString); // [수정] 안전하게 가져온 첫 글자를 파라미터로 전달
        charPrs(compose, hwpOverlappingLetter);
    }

    // [삭제] 더 이상 필요 없으며, 안전하지 않은 firstChar 메서드를 삭제합니다.
    /*
    private static String firstChar(ControlOverlappingLetter hwpOverlappingLetter) {
        return hwpOverlappingLetter.getHeader().getOverlappingLetterList().get(0).toUTF16LEString();
    }
    */

    private static ComposeCircleType composeCircleType(String firstChar) {
        switch (firstChar) {
            case "◯":
                return ComposeCircleType.SHAPE_CIRCLE;
            case "●":
                return ComposeCircleType.SHAPE_REVERSAL_CIRCLE;
            case "□":
                return ComposeCircleType.SHAPE_RECTANGLE;
            case "■":
                return ComposeCircleType.SHAPE_REVERSAL_RECTANGLE;
            case "△":
                return ComposeCircleType.SHAPE_TRIANGLE;
            case "▲":
                return ComposeCircleType.SHAPE_REVERSAL_TIRANGLE;
            case "☼":
                return ComposeCircleType.SHAPE_LIGHT;
            case "◇":
                return ComposeCircleType.SHAPE_RHOMBUS;
            case "◆":
                return ComposeCircleType.SHAPE_REVERSAL_RHOMBUS;
            case "▢":
                return ComposeCircleType.SHAPE_ROUNDED_RECTANGLE;
            case "♲":
                return ComposeCircleType.SHAPE_EMPTY_CIRCULATE_TRIANGLE;
            case "♺":
                return ComposeCircleType.SHAPE_THIN_CIRCULATE_TRIANGLE;
            case "♻":
                return ComposeCircleType.SHAPE_THICK_CIRCULATE_TRIANGLE;
        }
        return ComposeCircleType.CHAR;
    }

    private static ComposeType composeType(short expendInsideLetter) {
        switch (expendInsideLetter) {
            case 0:
                return ComposeType.SPREAD;
            case 1:
                return ComposeType.OVERLAP;
        }
        return ComposeType.SPREAD;
    }

    // [수정] 안전하게 가져온 첫 글자를 파라미터로 받도록 메서드 시그니처를 변경합니다.
    private static void composeText(Compose compose, ControlOverlappingLetter hwpOverlappingLetter, String firstChar) {
        StringBuilder sb = new StringBuilder();
        if (compose.circleType() == ComposeCircleType.CHAR && !firstChar.equals("　")) {
            for (HWPString hwpStr : hwpOverlappingLetter.getHeader().getOverlappingLetterList()) {
                sb.append(hwpStr.toUTF16LEString());
            }
        } else {
            boolean isFirst = true;
            for (HWPString hwpStr : hwpOverlappingLetter.getHeader().getOverlappingLetterList()) {
                if (isFirst) {
                    isFirst = false;
                } else {
                    sb.append(hwpStr.toUTF16LEString());
                }
            }
        }
        compose.composeText(sb.toString());
    }

    private static void charPrs(Compose compose, ControlOverlappingLetter hwpOverlappingLetter) {
        // [수정] 반복문을 돌리기 전, 리스트가 null이 아닌지 확인하는 방어 코드를 추가합니다.
        List<Long> charShapeIdList = hwpOverlappingLetter.getHeader().getCharShapeIdList();
        if (charShapeIdList != null) {
            for (Long id : charShapeIdList) {
                compose.addNewCharPr()
                        .prIDRef(ValueConvertor.refID(id));
            }
        }
    }
}

