package vn.javaweb.ComputerShop.repository.product;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import vn.javaweb.ComputerShop.domain.dto.request.ProductFilterDTO;
import vn.javaweb.ComputerShop.domain.dto.response.ProductAdRpDTO;
import vn.javaweb.ComputerShop.domain.dto.response.ProductRpDTO;

import java.util.ArrayList;
import java.util.List;

@Repository
public class ProductRepositoryCustomImpl implements ProductRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    public static void joinTable(ProductFilterDTO productFilterDTO , StringBuilder sql){

    }

    public static void queryNormal(ProductFilterDTO productFilterDTO , StringBuilder sql){

        if (productFilterDTO.getFactory() != null && !productFilterDTO.getFactory().isEmpty()) {
            sql.append(" AND (");  // Mở ngoặc cho các điều kiện OR
            for (int i = 0; i < productFilterDTO.getFactory().size(); i++) {
                String factory = productFilterDTO.getFactory().get(i).trim();
                if (i > 0) {
                    sql.append(" OR "); // Chỉ thêm OR từ phần tử thứ 2 trở đi
                }
                sql.append("products.factory = '").append(factory).append("'");
            }
            sql.append(")");  // Đóng ngoặc sau khi kết thúc các điều kiện OR
        }
        if ( productFilterDTO.getTarget() != null && !productFilterDTO.getTarget().isEmpty()){
            sql.append(" AND (");
            for ( int i = 0 ; i <  productFilterDTO.getTarget().size() ; i++){
                String target = productFilterDTO.getTarget().get(i).trim();
                if ( i > 0){
                    sql.append(" OR ");
                }
                sql.append(" products.target = '").append(target).append("'");

            }
            sql.append(")");
        }


    }

    public static void queryPriceUnion(ProductFilterDTO productFilterDTO , StringBuilder sql){


        if (productFilterDTO.getPrice() != null && !productFilterDTO.getPrice().isEmpty() ){
            List<String> unionQuery = new ArrayList<>();
            for ( String price :  productFilterDTO.getPrice() ) {
                double min = 0;
                double max = 0;

                switch (price.trim()) {
                    case "duoi-10-trieu":
                        min = 0;
                        max = 10000000;
                        break;
                    case "10-15-trieu":
                        min = 10000000;
                        max = 15000000;
                        break;
                    case "15-20-trieu":
                        min = 15000000;
                        max = 20000000;
                        break;
                    case "tren-20-trieu":
                        min = 20000000;
                        max = 200000000;
                        break;
                }

                StringBuilder subSql = new StringBuilder();
                subSql.append("SELECT products.id, products.name, products.image, products.short_desc, products.price ")
                        .append("FROM products WHERE 1=1 ");
                queryNormal(productFilterDTO , subSql);

                subSql.append(" AND products.price >= ").append(min).append(" AND products.price < ").append(max);
                unionQuery.add(subSql.toString());
            }

            // ngoai vong lap
//            sql.setLength(0); // reset sql gốc
            sql.append(String.join(" UNION ALL ", unionQuery));

        }else {

            sql.append("SELECT products.id, products.name, products.image, products.short_desc, products.price ")
                    .append("FROM products WHERE 1=1 ");
            queryNormal(productFilterDTO , sql);
        }
    }

    public static void querySpecial(ProductFilterDTO productFilterDTO , StringBuilder sql){
        String sortField = "price"; // Mặc định là 'price', vì dù là 'products.price' hay 'all_results.price' thì đều được alias đúng
        String sortTableAlias = sql.toString().contains("AS all_results") ? "all_results" : "products";

        if (productFilterDTO.getSort()!=null && !productFilterDTO.getSort().isEmpty()){
            switch (productFilterDTO.getSort().trim()){
                case "gia-tang-dan":
                    sql.append(" ORDER BY ").append(sortTableAlias).append(".").append(sortField).append(" ASC ");
                    break;
                case "gia-giam-dan":
                    sql.append(" ORDER BY ").append(sortTableAlias).append(".").append(sortField).append(" DESC ");
                    break;
            }

        }


    }



    @Override
    public Page<ProductRpDTO> findProductFilter(ProductFilterDTO productFilterDTO, Pageable pageable) {
        StringBuilder sql = new StringBuilder();
        boolean hasMultiplePriceRanges = productFilterDTO.getPrice() != null && productFilterDTO.getPrice().size() > 1;

        if (hasMultiplePriceRanges) {
            StringBuilder unionSql = new StringBuilder();
            queryPriceUnion(productFilterDTO, unionSql);
            sql = new StringBuilder("SELECT * FROM (").append(unionSql).append(") AS all_results");
            querySpecial(productFilterDTO, sql);
        }
        else  {
            queryPriceUnion(productFilterDTO, sql);
            querySpecial(productFilterDTO, sql);
        }


        // neu ma ko co price thi cai ham queryPriceUnion() no se tra ve sql trong tuc la nguoi dung ko set price
        if (sql.isEmpty()) {
            sql.append("SELECT products.id, products.name, products.image, products.short_desc, products.price FROM products WHERE 1=1 ");
            queryNormal(productFilterDTO, sql);
            querySpecial(productFilterDTO, sql);
        }


        String querySql = sql.toString();
        Query query = entityManager.createNativeQuery( querySql , ProductRpDTO.class);

        // Đếm số bản ghi( row ) => so page
        List<Object> rows = query.getResultList();
        long totalResults = rows.size();

//        offset = (pageNumber - 1) * pageSize).
        // setup pagination cho List<BuildingEntity>
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());
        List<ProductRpDTO> resultList = query.getResultList();



        return new PageImpl<>(resultList, pageable , totalResults);
        //page.getTotalPage() la so trang
    }

    @Override
    public Page<ProductAdRpDTO> findProducts(Pageable pageable) {
        String querySql = "SELECT products.id , products.name , products.price , products.factory FROM products";
        Query query = entityManager.createNativeQuery( querySql , ProductAdRpDTO.class);

        // Đếm số bản ghi( row ) => so page
        List<Object> rows = query.getResultList();
        long totalResults = rows.size();

        // setup pagination cho List<BuildingEntity>
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());
        List<ProductAdRpDTO> resultList = query.getResultList();



        return new PageImpl<>(resultList, pageable , totalResults);

    }
}


