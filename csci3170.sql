-- 5.1 System Interface

-- 5.1.1 Create table schemas in the database
CREATE TABLE book (
    ISBN VARCHAR2(13) PRIMARY KEY,
    title VARCHAR2(100) NOT NULL,
    unit_price NUMBER NOT NULL,
    no_of_copies NUMBER NOT NULL
);

CREATE TABLE book_author (
    ISBN VARCHAR2(13),
    author_name VARCHAR2(50),
    CONSTRAINT pk_book_author PRIMARY KEY (ISBN, author_name),
    CONSTRAINT fk_book_author_book FOREIGN KEY (ISBN) REFERENCES book(ISBN)
);

CREATE TABLE customer (
    customer_id VARCHAR2(10) PRIMARY KEY,
    name VARCHAR2(50) NOT NULL,
    shipping_address VARCHAR2(200) NOT NULL,
    credit_card_no VARCHAR2(19) NOT NULL
);

CREATE TABLE orders (
    order_id CHAR(8) PRIMARY KEY,
    order_date DATE NOT NULL,
    shipping_status CHAR(1) NOT NULL,
    charge NUMBER NOT NULL,
    customer_id VARCHAR2(10),
    CONSTRAINT fk_orders_customer FOREIGN KEY (customer_id) REFERENCES customer(customer_id)
);

CREATE TABLE ordering (
    order_id CHAR(8),
    ISBN VARCHAR2(13),
    quantity NUMBER NOT NULL,
    CONSTRAINT pk_ordering PRIMARY KEY (order_id, ISBN),
    CONSTRAINT fk_ordering_order FOREIGN KEY (order_id) REFERENCES orders(order_id),
    CONSTRAINT fk_ordering_book FOREIGN KEY (ISBN) REFERENCES book(ISBN)
);

-- 5.1.2 Delete table schemas in the database
-- DROP TABLE category CASCADE CONSTRAINTS;
-- DROP TABLE user CASCADE CONSTRAINTS;
-- DROP TABLE book CASCADE CONSTRAINTS;
-- DROP TABLE copy CASCADE CONSTRAINTS;
-- DROP TABLE checkout_record CASCADE CONSTRAINTS;
-- DROP TABLE author CASCADE CONSTRAINTS;

-- 5.1.3 Insert data to the database
INSERT INTO book VALUES ('1-1234-1234-1', 'Database I', 100, 50);
INSERT INTO book VALUES ('2-2345-2345-2', 'Database II', 110, 40);
INSERT INTO book VALUES ('3-3456-3456-3', 'Operating System', 130, 20);
INSERT INTO book VALUES ('4-4567-4567-4', 'Programming in C language', 140, 10);
INSERT INTO book VALUES ('5-5678-5678-5', 'Programming in Java language', 150, 5);

INSERT INTO customer VALUES ('adafu', 'Ada', '222,Shatin,Hong Kong', '4444-4444-4444-4444');
INSERT INTO customer VALUES ('cwwong', 'Raymond', '123,Shatin,Hong Kong', '1234-1234-1234-1234');
INSERT INTO customer VALUES ('hndai', 'Henry', '567,Shatin,Hong Kong', '1111-1111-1111-1111');
INSERT INTO customer VALUES ('hyyue', 'Willy', '234,Kwai Chung,Hong Kong', '4321-4321-4321-4321');
INSERT INTO customer VALUES ('raymond', 'Raymond Wong', '999,Tai Wai,Hong Kong', '9999-9999-9999-9999');
INSERT INTO customer VALUES ('twleung', 'Oscar', '890,Tin Shui Wai,Hong Kong', '2222-2222-2222-2222');
INSERT INTO customer VALUES ('wcliew', 'Alan', '333,Tsuen Wan,Hong Kong', '5555-5555-5555-5555');
INSERT INTO customer VALUES ('xcai', 'Teresa', '111,Shatin,Hong Kong', '3333-3333-3333-3333');

INSERT INTO book_author VALUES ('1-1234-1234-1', 'Ada');
INSERT INTO book_author VALUES ('1-1234-1234-1', 'Raymond');
INSERT INTO book_author VALUES ('1-1234-1234-1', 'Willy');
INSERT INTO book_author VALUES ('2-2345-2345-2', 'Henry');
INSERT INTO book_author VALUES ('2-2345-2345-2', 'Teresa');
INSERT INTO book_author VALUES ('3-3456-3456-3', 'Ada');
INSERT INTO book_author VALUES ('3-3456-3456-3', 'Alan');
INSERT INTO book_author VALUES ('4-4567-4567-4', 'Magic');
INSERT INTO book_author VALUES ('5-5678-5678-5', 'Oscar');

INSERT INTO orders VALUES ('00000000', TO_DATE('2005-09-01', 'YYYY-MM-DD'), 'Y', 120, 'cwwong');
INSERT INTO orders VALUES ('00000001', TO_DATE('2005-09-02', 'YYYY-MM-DD'), 'Y', 120, 'cwwong');
INSERT INTO orders VALUES ('00000002', TO_DATE('2005-09-07', 'YYYY-MM-DD'), 'N', 120, 'hyyue');
INSERT INTO orders VALUES ('00000003', TO_DATE('2005-09-10', 'YYYY-MM-DD'), 'N', 120, 'hyyue');
INSERT INTO orders VALUES ('00000004', TO_DATE('2005-09-12', 'YYYY-MM-DD'), 'N', 120, 'hndai');
INSERT INTO orders VALUES ('00000005', TO_DATE('2005-09-20', 'YYYY-MM-DD'), 'N', 120, 'hndai');
INSERT INTO orders VALUES ('00000006', TO_DATE('2005-09-30', 'YYYY-MM-DD'), 'N', 120, 'twleung');
INSERT INTO orders VALUES ('00000007', TO_DATE('2005-10-01', 'YYYY-MM-DD'), 'Y', 130, 'twleung');
INSERT INTO orders VALUES ('00000008', TO_DATE('2005-10-06', 'YYYY-MM-DD'), 'Y', 130, 'xcai');
INSERT INTO orders VALUES ('00000009', TO_DATE('2005-10-09', 'YYYY-MM-DD'), 'Y', 130, 'xcai');
INSERT INTO orders VALUES ('00000010', TO_DATE('2005-10-13', 'YYYY-MM-DD'), 'Y', 320, 'xcai');

INSERT INTO ordering VALUES ('00000000', '1-1234-1234-1', 1);
INSERT INTO ordering VALUES ('00000001', '1-1234-1234-1', 1);
INSERT INTO ordering VALUES ('00000002', '1-1234-1234-1', 1);
INSERT INTO ordering VALUES ('00000003', '1-1234-1234-1', 1);
INSERT INTO ordering VALUES ('00000004', '1-1234-1234-1', 1);
INSERT INTO ordering VALUES ('00000005', '1-1234-1234-1', 1);
INSERT INTO ordering VALUES ('00000006', '1-1234-1234-1', 1);
INSERT INTO ordering VALUES ('00000007', '2-2345-2345-2', 1);
INSERT INTO ordering VALUES ('00000008', '2-2345-2345-2', 1);
INSERT INTO ordering VALUES ('00000009', '2-2345-2345-2', 1);
INSERT INTO ordering VALUES ('00000010', '4-4567-4567-4', 1);
INSERT INTO ordering VALUES ('00000010', '5-5678-5678-5', 1);

-- 5.1.4 System Date Setting(TODO in app)

-- 5.2 Customer Interface

-- 5.2.1.1 Book Search Query by ISBN
SELECT b.title, b.ISBN, b.unit_price, b.no_of_copies, LISTAGG(ba.author_name, ', ') WITHIN GROUP (ORDER BY ba.author_name) AS authors
FROM book b
JOIN book_author ba ON b.ISBN = ba.ISBN
WHERE b.ISBN = 'ISBN_to_search'
GROUP BY b.title, b.ISBN, b.unit_price, b.no_of_copies
ORDER BY b.title, b.ISBN;

-- 5.2.1.2 Book Search Query by Book Title
SELECT b.title, b.ISBN, b.unit_price, b.no_of_copies, LISTAGG(ba.author_name, ', ') WITHIN GROUP (ORDER BY ba.author_name) AS authors
FROM book b
JOIN book_author ba ON b.ISBN = ba.ISBN
WHERE LOWER(b.title) LIKE LOWER('Book_Title_to_search') -- Use '%' for wildcard searches
GROUP BY b.title, b.ISBN, b.unit_price, b.no_of_copies
ORDER BY b.title, b.ISBN;

-- 5.2.1.3 Book Search Query by Author Name
SELECT b.title, b.ISBN, b.unit_price, b.no_of_copies, LISTAGG(ba.author_name, ', ') WITHIN GROUP (ORDER BY ba.author_name) AS authors
FROM book b
JOIN book_author ba ON b.ISBN = ba.ISBN
WHERE LOWER(ba.author_name) LIKE LOWER('Author_Name_to_search') -- Use '%' for wildcard searches
GROUP BY b.title, b.ISBN, b.unit_price, b.no_of_copies
ORDER BY b.title, b.ISBN;

-- 5.2.2 Order Creation(TODO in app)
-- Check Book Availability
SELECT no_of_copies FROM book WHERE ISBN = 'ISBN_to_check';
-- Current Greatest 'order_id' for later use ing app
SELECT MAX(TO_NUMBER(order_id)) FROM orders;
-- Inserting a New Order(calculate the total charge in PL)
INSERT INTO orders (order_id, order_date, shipping_status, charge, customer_id)
VALUES ('Next_Order_ID', CURRENT_DATE, 'N', Calculated_Charge, 'Customer_ID');
-- Inserting Ordered Books(Each book)
INSERT INTO ordering (order_id, ISBN, quantity)
VALUES ('Order_ID', 'ISBN_of_book', Quantity);
-- Update Book Copies
UPDATE book SET no_of_copies = no_of_copies - Quantity WHERE ISBN = 'ISBN_of_book';

-- 5.2.3 Order Altering(TODO in app)
-- Add Copies of Book
UPDATE ordering SET quantity = quantity + Additional_Quantity
WHERE order_id = 'Order_ID' AND ISBN = 'ISBN_of_book';

UPDATE book SET no_of_copies = no_of_copies - Additional_Quantity
WHERE ISBN = 'ISBN_of_book';
-- Remove Copies of Book
UPDATE ordering SET quantity = quantity - Removed_Quantity
WHERE order_id = 'Order_ID' AND ISBN = 'ISBN_of_book';

UPDATE book SET no_of_copies = no_of_copies + Removed_Quantity
WHERE ISBN = 'ISBN_of_book';

-- 5.2.4 Order Query
SELECT o.order_id, o.order_date, LISTAGG(b.title || ' (' || ob.quantity || ' copies)', ', ') WITHIN GROUP (ORDER BY b.title) AS books_ordered, o.charge, o.shipping_status
FROM orders o
JOIN ordering ob ON o.order_id = ob.order_id
JOIN book b ON ob.ISBN = b.ISBN
WHERE o.customer_id = 'Customer_ID_to_search' AND EXTRACT(YEAR FROM o.order_date) = Year_to_search
GROUP BY o.order_id, o.order_date, o.charge, o.shipping_status
ORDER BY o.order_id;

-- 5.3 Bookstore Interface

-- 5.3.1 Order Update
UPDATE orders
SET shipping_status = 'Y'
WHERE order_id = 'Order_ID_to_update' AND shipping_status = 'N'
AND EXISTS (
  SELECT 1
  FROM ordering
  WHERE order_id = 'Order_ID_to_update' AND quantity >= 1
);

-- 5.3.2 Order Query
-- List of Orders in the Month
SELECT order_id, customer_id, order_date, charge
FROM orders
WHERE shipping_status = 'Y'
AND TO_CHAR(order_date, 'YYYY-MM') = 'YYYY-MM_to_query'
ORDER BY order_id;
-- Total Charge in the Month
SELECT SUM(charge) AS total_charge
FROM orders
WHERE shipping_status = 'Y'
AND TO_CHAR(order_date, 'YYYY-MM') = 'YYYY-MM_to_query';

-- 5.3.3 N Most Popular Book Query
SELECT b.title, b.ISBN, SUM(o.quantity) AS total_ordered
FROM ordering o
JOIN book b ON o.ISBN = b.ISBN
GROUP BY b.title, b.ISBN
ORDER BY total_ordered DESC, b.title, b.ISBN
FETCH FIRST N ROWS ONLY;