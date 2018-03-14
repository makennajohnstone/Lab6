select * from lab6_rooms
  join lab6_reservations on RoomCode = Room
  where RoomCode not in
    (select RoomCode from lab6_rooms
      join lab6_reservations on RoomCode = Room
      where (Checkout >= STR_TO_DATE('2018-10-26', '%Y-%m-%d') and CheckIn < STR_TO_DATE('2018-10-28', '%Y-%m-%d'))
    )
;

insert into mejohnst.lab6_reservations (CODE, Room, CheckIn, CheckOut, Rate, LastName, FirstName, Adults, Kids)
  values(99864, 'HBB', STR_TO_DATE('2018-03-19', '%Y-%m-%d'), STR_TO_DATE('2018-03-18', '%Y-%m-%d'), 410.02, 'Johnstone', 'Makenna', 2, 0);

  SELECT lab6_rooms.RoomCode, (SELECT
     CASE
     WHEN not exists (
       
       SELECT second.CheckIn
       FROM lab6_reservations second
       WHERE second.Room = first.Room AND second.Checkout > CURDATE() AND second.CheckIn <= CURDATE()
     ) THEN CURDATE()
      

     ELSE min(Checkout)
     END

     FROM lab6_reservations first
     WHERE first.Room = lab6_rooms.RoomCode AND first.CheckIn >= CURDATE() AND not exists (
       SELECT *  -- with EXISTS, projection doesn't matter, could be any column or all
       FROM lab6_reservations third
       WHERE third.Room = first.Room and third.CheckIn = first.CheckOut
     )

    ) as FirstAvailable
  from lab6_rooms;



-- find the rooms that do not have a reservation in the range.

Select *
from lab6_reservations RE1
WHERE ('2018-01-01' not between RE1.CheckIn AND RE1.CheckOut) AND ('2018-01-05' not between RE1.CheckIn AND RE1.CheckOut)

Select DISTINCT RE1.Room
from lab6_reservations RE1
WHERE RE1.Room not IN (
  Select RE2.Room
  from lab6_reservations RE2
  WHERE (('2018-02-19' between RE2.CheckIn AND DATE_ADD(RE2.Checkout, INTERVAL -1 DAY)) OR (('2018-03-04' between DATE_ADD(RE2.CheckIn, INTERVAL +1 DAY) AND RE2.Checkout))));
