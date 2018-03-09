select * from lab6_rooms
  join lab6_reservations on RoomCode = Room
  where RoomCode not in
    (select RoomCode from lab6_rooms
      join lab6_reservations on RoomCode = Room
      where (Checkout >= STR_TO_DATE('2018-10-26', '%Y-%m-%d') and CheckIn < STR_TO_DATE('2018-10-28', '%Y-%m-%d'))
    )
;
