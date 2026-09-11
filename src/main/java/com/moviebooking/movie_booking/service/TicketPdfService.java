package com.moviebooking.movie_booking.service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.moviebooking.movie_booking.model.Booking;

@Service
public class TicketPdfService {

    private static final Color PINK =
            new Color(248, 68, 100);

    private static final Color DARK =
            new Color(24, 24, 27);

    private static final Color LIGHT_GRAY =
            new Color(245, 245, 247);

    private static final Color BORDER =
            new Color(225, 225, 230);

    private static final Color TEXT_GRAY =
            new Color(105, 105, 110);

    private static final Color GREEN =
            new Color(25, 135, 84);

    public byte[] generateTicket(Booking booking) {

        ByteArrayOutputStream outputStream =
                new ByteArrayOutputStream();

        try {

            Document document =
                    new Document(
                            PageSize.A4,
                            42,
                            42,
                            40,
                            40
                    );

            PdfWriter.getInstance(
                    document,
                    outputStream
            );

            document.open();

            /*
             * ======================================
             * HEADER
             * ======================================
             */

            PdfPTable header =
                    new PdfPTable(1);

            header.setWidthPercentage(100);

            PdfPCell headerCell =
                    new PdfPCell();

            headerCell.setBackgroundColor(DARK);

            headerCell.setBorder(
                    Rectangle.NO_BORDER
            );

            headerCell.setPaddingTop(22);
            headerCell.setPaddingBottom(22);
            headerCell.setPaddingLeft(22);

            Font brandFont =
                    new Font(
                            Font.HELVETICA,
                            23,
                            Font.BOLD,
                            PINK
                    );

            Paragraph brand =
                    new Paragraph(
                            "MOVIEBOOKING",
                            brandFont
                    );

            brand.setSpacingAfter(5);

            headerCell.addElement(brand);

            Font subtitleFont =
                    new Font(
                            Font.HELVETICA,
                            10,
                            Font.NORMAL,
                            Color.WHITE
                    );

            headerCell.addElement(
                    new Paragraph(
                            "Your Movie Ticket",
                            subtitleFont
                    )
            );

            header.addCell(headerCell);

            document.add(header);

            /*
             * ======================================
             * TICKET CONFIRMATION
             * ======================================
             */

            PdfPTable confirmation =
                    new PdfPTable(
                            new float[]{3f, 1f}
                    );

            confirmation.setWidthPercentage(100);
            confirmation.setSpacingBefore(18);

            PdfPCell confirmationLeft =
                    new PdfPCell();

            confirmationLeft.setBorder(
                    Rectangle.NO_BORDER
            );

            confirmationLeft.setPadding(8);

            Font confirmedTitle =
                    new Font(
                            Font.HELVETICA,
                            17,
                            Font.BOLD,
                            DARK
                    );

            confirmationLeft.addElement(
                    new Paragraph(
                            "Booking Confirmed",
                            confirmedTitle
                    )
            );

            Font referenceFont =
                    new Font(
                            Font.HELVETICA,
                            9,
                            Font.NORMAL,
                            TEXT_GRAY
                    );

            confirmationLeft.addElement(
                    new Paragraph(
                            "Booking ID: #"
                                    + booking.getId(),
                            referenceFont
                    )
            );

            confirmation.addCell(
                    confirmationLeft
            );

            PdfPCell statusCell =
                    new PdfPCell(
                            new Phrase(
                                    booking.getStatus(),
                                    new Font(
                                            Font.HELVETICA,
                                            10,
                                            Font.BOLD,
                                            GREEN
                                    )
                            )
                    );

            statusCell.setHorizontalAlignment(
                    Element.ALIGN_CENTER
            );

            statusCell.setVerticalAlignment(
                    Element.ALIGN_MIDDLE
            );

            statusCell.setBackgroundColor(
                    new Color(
                            232,
                            248,
                            238
                    )
            );

            statusCell.setBorderColor(
                    new Color(
                            190,
                            230,
                            205
                    )
            );

            statusCell.setPadding(10);

            confirmation.addCell(statusCell);

            document.add(confirmation);

            /*
             * ======================================
             * MOVIE TITLE
             * ======================================
             */

            Paragraph movieTitle =
                    new Paragraph(
                            booking.getShow()
                                    .getMovie()
                                    .getTitle(),
                            new Font(
                                    Font.HELVETICA,
                                    26,
                                    Font.BOLD,
                                    DARK
                            )
                    );

            movieTitle.setSpacingBefore(20);
            movieTitle.setSpacingAfter(5);

            document.add(movieTitle);

            Paragraph movieInfo =
                    new Paragraph(
                            safe(
                                    booking
                                            .getShow()
                                            .getMovie()
                                            .getGenre()
                            )
                                    + "   |   "
                                    + safe(
                                    booking
                                            .getShow()
                                            .getMovie()
                                            .getLanguage()
                            ),
                            new Font(
                                    Font.HELVETICA,
                                    10,
                                    Font.NORMAL,
                                    TEXT_GRAY
                            )
                    );

            movieInfo.setSpacingAfter(20);

            document.add(movieInfo);

            /*
             * ======================================
             * SHOW DETAILS
             * ======================================
             */

            PdfPTable details =
                    new PdfPTable(
                            new float[]{
                                    1f,
                                    1f
                            }
                    );

            details.setWidthPercentage(100);

            addDetail(
                    details,
                    "THEATRE",
                    booking.getShow()
                            .getTheatre()
                            .getName()
            );

            addDetail(
                    details,
                    "SCREEN",
                    booking.getShow()
                            .getScreen()
                            .getScreenName()
            );

            addDetail(
                    details,
                    "DATE",
                    booking.getShow()
                            .getShowDate()
                            .toString()
            );

            addDetail(
                    details,
                    "TIME",
                    booking.getShow()
                            .getShowTime()
                            .toString()
            );

            document.add(details);

            /*
             * ======================================
             * SEATS
             * ======================================
             */

            String seatNumbers =
                    booking.getSeats()
                            .stream()
                            .map(seat ->
                                    seat.getSeatNumber()
                            )
                            .collect(
                                    Collectors.joining(
                                            ", "
                                    )
                            );

            PdfPTable seatTable =
                    new PdfPTable(1);

            seatTable.setWidthPercentage(100);
            seatTable.setSpacingBefore(18);

            PdfPCell seatCell =
                    new PdfPCell();

            seatCell.setBackgroundColor(
                    LIGHT_GRAY
            );

            seatCell.setBorderColor(
                    BORDER
            );

            seatCell.setPadding(15);

            seatCell.addElement(
                    new Paragraph(
                            "SEATS",
                            new Font(
                                    Font.HELVETICA,
                                    9,
                                    Font.BOLD,
                                    TEXT_GRAY
                            )
                    )
            );

            Paragraph seatText =
                    new Paragraph(
                            seatNumbers,
                            new Font(
                                    Font.HELVETICA,
                                    18,
                                    Font.BOLD,
                                    PINK
                            )
                    );

            seatText.setSpacingBefore(5);

            seatCell.addElement(
                    seatText
            );

            seatTable.addCell(
                    seatCell
            );

            document.add(seatTable);

            /*
             * ======================================
             * CUSTOMER
             * ======================================
             */

            PdfPTable customerTable =
                    new PdfPTable(
                            new float[]{1f, 1f}
                    );

            customerTable.setWidthPercentage(
                    100
            );

            customerTable.setSpacingBefore(
                    18
            );

            addDetail(
                    customerTable,
                    "BOOKED BY",
                    booking.getUser()
                            .getName()
            );

            addDetail(
                    customerTable,
                    "EMAIL",
                    booking.getUser()
                            .getEmail()
            );

            document.add(customerTable);

            /*
             * ======================================
             * PRICE
             * ======================================
             */

            PdfPTable totalTable =
                    new PdfPTable(
                            new float[]{2f, 1f}
                    );

            totalTable.setWidthPercentage(
                    100
            );

            totalTable.setSpacingBefore(
                    20
            );

            PdfPCell totalLabel =
                    new PdfPCell(
                            new Phrase(
                                    "TOTAL AMOUNT",
                                    new Font(
                                            Font.HELVETICA,
                                            11,
                                            Font.BOLD,
                                            Color.WHITE
                                    )
                            )
                    );

            totalLabel.setBackgroundColor(
                    DARK
            );

            totalLabel.setBorder(
                    Rectangle.NO_BORDER
            );

            totalLabel.setPadding(16);

            totalLabel.setVerticalAlignment(
                    Element.ALIGN_MIDDLE
            );

            totalTable.addCell(
                    totalLabel
            );

            PdfPCell totalValue =
                    new PdfPCell(
                            new Phrase(
                                    "Rs. "
                                            + String.format(
                                            "%.2f",
                                            booking
                                                    .getTotalAmount()
                                    ),
                                    new Font(
                                            Font.HELVETICA,
                                            17,
                                            Font.BOLD,
                                            Color.WHITE
                                    )
                            )
                    );

            totalValue.setBackgroundColor(
                    PINK
            );

            totalValue.setBorder(
                    Rectangle.NO_BORDER
            );

            totalValue.setPadding(16);

            totalValue.setHorizontalAlignment(
                    Element.ALIGN_RIGHT
            );

            totalTable.addCell(
                    totalValue
            );

            document.add(totalTable);

            /*
             * ======================================
             * BOOKING DATE
             * ======================================
             */

            Paragraph bookedOn =
                    new Paragraph(
                            "Booked on: "
                                    + booking
                                    .getBookingTime(),
                            new Font(
                                    Font.HELVETICA,
                                    9,
                                    Font.NORMAL,
                                    TEXT_GRAY
                            )
                    );

            bookedOn.setAlignment(
                    Element.ALIGN_RIGHT
            );

            bookedOn.setSpacingBefore(12);

            document.add(bookedOn);

            /*
             * ======================================
             * DIVIDER
             * ======================================
             */

            PdfPTable divider =
                    new PdfPTable(1);

            divider.setWidthPercentage(100);
            divider.setSpacingBefore(22);
            divider.setSpacingAfter(16);

            PdfPCell dividerCell =
                    new PdfPCell(
                            new Phrase("")
                    );

            dividerCell.setBorder(
                    Rectangle.BOTTOM
            );

            dividerCell.setBorderColor(
                    BORDER
            );

            dividerCell.setFixedHeight(5);

            divider.addCell(
                    dividerCell
            );

            document.add(divider);

            /*
             * ======================================
             * IMPORTANT MESSAGE
             * ======================================
             */

            Font noteTitle =
                    new Font(
                            Font.HELVETICA,
                            10,
                            Font.BOLD,
                            DARK
                    );

            Paragraph note =
                    new Paragraph(
                            "IMPORTANT",
                            noteTitle
                    );

            document.add(note);

            Paragraph instructions =
                    new Paragraph(
                            "Please carry this ticket when entering the theatre. "
                                    + "Seats are valid only for the movie, date, time and screen shown above.",
                            new Font(
                                    Font.HELVETICA,
                                    9,
                                    Font.NORMAL,
                                    TEXT_GRAY
                            )
                    );

            instructions.setLeading(15);

            document.add(instructions);

            /*
             * ======================================
             * FOOTER
             * ======================================
             */

            PdfPTable footer =
                    new PdfPTable(1);

            footer.setWidthPercentage(100);
            footer.setSpacingBefore(30);

            PdfPCell footerCell =
                    new PdfPCell();

            footerCell.setBackgroundColor(
                    LIGHT_GRAY
            );

            footerCell.setBorder(
                    Rectangle.NO_BORDER
            );

            footerCell.setPadding(15);

            footerCell.setHorizontalAlignment(
                    Element.ALIGN_CENTER
            );

            Paragraph thankYou =
                    new Paragraph(
                            "Thank you for booking with MovieBooking",
                            new Font(
                                    Font.HELVETICA,
                                    10,
                                    Font.BOLD,
                                    PINK
                            )
                    );

            thankYou.setAlignment(
                    Element.ALIGN_CENTER
            );

            footerCell.addElement(
                    thankYou
            );

            Paragraph footerText =
                    new Paragraph(
                            "Enjoy your movie!",
                            new Font(
                                    Font.HELVETICA,
                                    8,
                                    Font.NORMAL,
                                    TEXT_GRAY
                            )
                    );

            footerText.setAlignment(
                    Element.ALIGN_CENTER
            );

            footerCell.addElement(
                    footerText
            );

            footer.addCell(
                    footerCell
            );

            document.add(footer);

            document.close();

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to generate ticket PDF",
                    e
            );
        }

        return outputStream.toByteArray();
    }

    /*
     * ======================================
     * DETAIL CELL
     * ======================================
     */

    private void addDetail(
            PdfPTable table,
            String label,
            String value) {

        PdfPCell cell =
                new PdfPCell();

        cell.setBorderColor(
                BORDER
        );

        cell.setBackgroundColor(
                Color.WHITE
        );

        cell.setPadding(13);

        Paragraph labelParagraph =
                new Paragraph(
                        label,
                        new Font(
                                Font.HELVETICA,
                                8,
                                Font.BOLD,
                                TEXT_GRAY
                        )
                );

        cell.addElement(
                labelParagraph
        );

        Paragraph valueParagraph =
                new Paragraph(
                        safe(value),
                        new Font(
                                Font.HELVETICA,
                                12,
                                Font.BOLD,
                                DARK
                        )
                );

        valueParagraph.setSpacingBefore(4);

        cell.addElement(
                valueParagraph
        );

        table.addCell(
                cell
        );
    }

    private String safe(String value) {

        return value == null
                ? "N/A"
                : value;
    }
}