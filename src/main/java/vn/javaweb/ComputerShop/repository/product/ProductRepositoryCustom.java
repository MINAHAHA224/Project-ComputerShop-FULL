package vn.javaweb.ComputerShop.repository.product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.javaweb.ComputerShop.domain.dto.request.ProductFilterDTO;
import vn.javaweb.ComputerShop.domain.dto.response.ProductAdRpDTO;
import vn.javaweb.ComputerShop.domain.dto.response.ProductRpDTO;

public interface ProductRepositoryCustom {

    Page<ProductRpDTO> findProductFilter (ProductFilterDTO productFilterDTO , Pageable pageable);

    Page<ProductAdRpDTO> findProducts ( Pageable pageable);
}
