--SP Feature Export Orders

CREATE
    DEFINER = `root`@`%` PROCEDURE `ExportDataOrders`(dateBegin_in DATETIME, dateEnd_in DATETIME,
                                                      statusOrder_in VARCHAR(255))
BEGIN
    WITH customer_orders AS (SELECT u.id, u.full_name, u.email
                             FROM users u),
         orderDetai_orders AS (SELECT od.order_id,
                                      GROUP_CONCAT(CONCAT(pr.name, ' ( SL : ', od.quantity, ') ') SEPARATOR
                                                   '| ') AS pr_detail
                               FROM order_detail od
                                        INNER JOIN (SELECT id, name FROM products) AS pr ON pr.id = od.product_id
                               GROUP BY od.order_id)
    SELECT o.id             order_id,
           o.time           order_time,
           o.total_price,
           o.receiver_name,
           o.receiver_phone,
           o.receiver_address,
           o.status         order_status,
           o.type_payment,
           o.status_payment,
           odo.pr_detail AS product_details
    FROM orders o USE INDEX (IX_TIME_ID_ORDERS)
             INNER JOIN customer_orders co ON co.id = o.user_id
             INNER JOIN orderDetai_orders odo ON odo.order_id = o.id
    WHERE (dateBegin_in IS NULL OR o.time > dateBegin_in)
      AND (dateEnd_in IS NULL OR o.time < dateEnd_in)
      AND (statusOrder_in IS NULL OR o.status = statusOrder_in)
    ORDER BY o.time, o.id;
END


--SP Feature Export User

CREATE
    DEFINER = `root`@`%` PROCEDURE `ExportDataUsers`(roleId_in BIGINT)
BEGIN
    WITH role_option AS (SELECT r.id, r.name
                         FROM roles r),
         auth_option AS (SELECT au.id, au.user_id, au.login_type
                         FROM auth_method au)
    SELECT u.id,
           u.full_name   AS fullName,
           u.email,
           u.phone,
           u.address,
           ro.name       AS role_name,
           ao.login_type AS auth_methods
    FROM users u USE INDEX (IX_FULLNAME_ID_USERNAME)
             INNER JOIN role_option ro ON ro.id = u.role_id
             LEFT JOIN auth_option ao ON u.id = ao.user_id
    WHERE (roleId_in IS NULL OR u.role_id = roleId_in)
    ORDER BY u.full_name, u.id;
END

--SP Feature Export Products

CREATE
    DEFINER = `root`@`%` PROCEDURE `ExportDataProduct`(IN factory_in VARCHAR(255))
BEGIN
    IF (factory_in IS NOT NULL AND factory_in <> '') THEN
        SELECT pr.id, pr.name, pr.price, pr.short_desc, pr.quantity, pr.sold
        FROM products pr USE INDEX (IX_FACTORY_PRODUCTS, IX_NAME_PRODUCTS)
        WHERE pr.factory = factory_in
        ORDER BY pr.name ASC;
    ELSE
        SELECT pr.id, pr.name, pr.price, pr.short_desc, pr.quantity, pr.sold
        FROM products pr USE INDEX (IX_NAME_PRODUCTS)
        ORDER BY pr.name ASC;
    END IF;
END
